package blue.lang.html.parser;

import blue.lang.html.Html;
import blue.lang.html.helper.Validate;
import blue.lang.html.nodes.*;

import java.util.List;

/**
 * Use the {@code XmlTreeBuilder} when you want to parse XML without any of the HTML DOM rules being applied to the
 * document.
 * <p>Usage example: {@code Document xmlDoc = Jsoup.parse(html, baseUrl, Parser.xmlParser());}</p>
 *
 * @author Jonathan Hedley
 */
public class XmlTreeBuilder extends TreeBuilder {
    ParseSettings defaultSettings() {
        return ParseSettings.preserveCase;
    }

    HtmlDocument parse(String input, String baseUri) {
        return parse(input, baseUri, ParseErrorList.noTracking(), ParseSettings.preserveCase);
    }

    @Override
    protected void initialiseParse(String input, String baseUri, ParseErrorList errors, ParseSettings settings) {
        super.initialiseParse(input, baseUri, errors, settings);
        stack.add(doc); // place the document onto the stack. differs from HtmlTreeBuilder (not on stack)
        doc.outputSettings().syntax(HtmlDocument.OutputSettings.Syntax.xml);
    }

    @Override
    protected boolean process(Token token) {
        // start tag, end tag, doctype, comment, character, eof
        switch (token.type) {
            case StartTag:
                insert(token.asStartTag());
                break;
            case EndTag:
                popStackToClose(token.asEndTag());
                break;
            case Comment:
                insert(token.asComment());
                break;
            case Character:
                insert(token.asCharacter());
                break;
            case Doctype:
                insert(token.asDoctype());
                break;
            case EOF: // could put some normalisation here if desired
                break;
            default:
                Validate.fail("Unexpected token type: " + token.type);
        }
        return true;
    }

    private void insertNode(Node node) {
        currentElement().appendChild(node);
    }

    HtmlElement insert(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name(), settings);
        // todo: wonder if for xml parsing, should treat all tags as unknown? because it's not html.
        HtmlElement el = new HtmlElement(tag, baseUri, settings.normalizeAttributes(startTag.attributes));
        insertNode(el);
        if (startTag.isSelfClosing()) {
            tokeniser.acknowledgeSelfClosingFlag();
            if (!tag.isKnownTag()) // unknown tag, remember this is self closing for output. see above.
                tag.setSelfClosing();
        } else {
            stack.add(el);
        }
        return el;
    }

    void insert(Token.Comment commentToken) {
        Comment comment = new Comment(commentToken.getData(), baseUri);
        Node insert = comment;
        if (commentToken.bogus) { // xml declarations are emitted as bogus comments (which is right for html, but not xml)
            // so we do a bit of a hack and parse the data as an element to pull the attributes out
            String data = comment.getData();
            if (data.length() > 1 && (data.startsWith("!") || data.startsWith("?"))) {
                HtmlDocument doc = Html.parse("<" + data.substring(1, data.length() -1) + ">", baseUri, Parser.xmlParser());
                HtmlElement el = doc.child(0);
                insert = new XmlDeclaration(settings.normalizeTag(el.tagName()), comment.baseUri(), data.startsWith("!"));
                insert.attributes().addAll(el.attributes());
            }
        }
        insertNode(insert);
    }

    void insert(Token.Character characterToken) {
        Node node = new TextNode(characterToken.getData(), baseUri);
        insertNode(node);
    }

    void insert(Token.Doctype d) {
        DocumentType doctypeNode = new DocumentType(settings.normalizeTag(d.getName()), d.getPubSysKey(), d.getPublicIdentifier(), d.getSystemIdentifier(), baseUri);
        insertNode(doctypeNode);
    }

    /**
     * If the stack contains an element with this tag's name, pop up the stack to remove the first occurrence. If not
     * found, skips.
     *
     * @param endTag
     */
    private void popStackToClose(Token.EndTag endTag) {
        String elName = endTag.name();
        HtmlElement firstFound = null;

        for (int pos = stack.size() -1; pos >= 0; pos--) {
            HtmlElement next = stack.get(pos);
            if (next.nodeName().equals(elName)) {
                firstFound = next;
                break;
            }
        }
        if (firstFound == null)
            return; // not found, skip

        for (int pos = stack.size() -1; pos >= 0; pos--) {
            HtmlElement next = stack.get(pos);
            stack.remove(pos);
            if (next == firstFound)
                break;
        }
    }

    List<Node> parseFragment(String inputFragment, String baseUri, ParseErrorList errors, ParseSettings settings) {
        initialiseParse(inputFragment, baseUri, errors, settings);
        runParser();
        return doc.childNodes();
    }
}
