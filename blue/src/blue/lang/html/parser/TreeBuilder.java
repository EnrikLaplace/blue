package blue.lang.html.parser;

import blue.lang.html.helper.Validate;
import blue.lang.html.nodes.Attributes;
import blue.lang.html.nodes.HtmlDocument;
import blue.lang.html.nodes.HtmlElement;

import java.util.ArrayList;

/**
 * @author Jonathan Hedley
 */
abstract class TreeBuilder {
    CharacterReader reader;
    Tokeniser tokeniser;
    protected HtmlDocument doc; // current doc we are building into
    protected ArrayList<HtmlElement> stack; // the stack of open elements
    protected String baseUri; // current base uri, for creating new elements
    protected Token currentToken; // currentToken is used only for error tracking.
    protected ParseErrorList errors; // null when not tracking errors
    protected ParseSettings settings;

    private Token.StartTag start = new Token.StartTag(); // start tag to process
    private Token.EndTag end  = new Token.EndTag();

    abstract ParseSettings defaultSettings();

    protected void initialiseParse(String input, String baseUri, ParseErrorList errors, ParseSettings settings) {
        Validate.notNull(input, "String input must not be null");
        Validate.notNull(baseUri, "BaseURI must not be null");

        doc = new HtmlDocument(baseUri);
        this.settings = settings;
        reader = new CharacterReader(input);
        this.errors = errors;
        tokeniser = new Tokeniser(reader, errors);
        stack = new ArrayList<HtmlElement>(32);
        this.baseUri = baseUri;
    }

    HtmlDocument parse(String input, String baseUri, ParseErrorList errors, ParseSettings settings) {
        initialiseParse(input, baseUri, errors, settings);
        runParser();
        return doc;
    }

    protected void runParser() {
        while (true) {
            Token token = tokeniser.read();
            process(token);
            token.reset();

            if (token.type == Token.TokenType.EOF)
                break;
        }
    }

    protected abstract boolean process(Token token);

    protected boolean processStartTag(String name) {
        if (currentToken == start) { // don't recycle an in-use token
            return process(new Token.StartTag().name(name));
        }
        return process(start.reset().name(name));
    }

    public boolean processStartTag(String name, Attributes attrs) {
        if (currentToken == start) { // don't recycle an in-use token
            return process(new Token.StartTag().nameAttr(name, attrs));
        }
        start.reset();
        start.nameAttr(name, attrs);
        return process(start);
    }

    protected boolean processEndTag(String name) {
        if (currentToken == end) { // don't recycle an in-use token
            return process(new Token.EndTag().name(name));
        }
        return process(end.reset().name(name));
    }


    protected HtmlElement currentElement() {
        int size = stack.size();
        return size > 0 ? stack.get(size-1) : null;
    }
}
