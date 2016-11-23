package blue.lang.html.select;

import blue.lang.html.nodes.HtmlElement;

/**
 * Base structural evaluator.
 */
abstract class StructuralEvaluator extends Evaluator {
    Evaluator evaluator;

    static class Root extends Evaluator {
        public boolean matches(HtmlElement root, HtmlElement element) {
            return root == element;
        }
    }

    static class Has extends StructuralEvaluator {
        public Has(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(HtmlElement root, HtmlElement element) {
            for (HtmlElement e : element.getAllElements()) {
                if (e != element && evaluator.matches(root, e))
                    return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(":has(%s)", evaluator);
        }
    }

    static class Not extends StructuralEvaluator {
        public Not(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(HtmlElement root, HtmlElement node) {
            return !evaluator.matches(root, node);
        }

        @Override
        public String toString() {
            return String.format(":not%s", evaluator);
        }
    }

    static class Parent extends StructuralEvaluator {
        public Parent(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(HtmlElement root, HtmlElement element) {
            if (root == element)
                return false;

            HtmlElement parent = element.parent();
            while (true) {
                if (evaluator.matches(root, parent))
                    return true;
                if (parent == root)
                    break;
                parent = parent.parent();
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(":parent%s", evaluator);
        }
    }

    static class ImmediateParent extends StructuralEvaluator {
        public ImmediateParent(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(HtmlElement root, HtmlElement element) {
            if (root == element)
                return false;

            HtmlElement parent = element.parent();
            return parent != null && evaluator.matches(root, parent);
        }

        @Override
        public String toString() {
            return String.format(":ImmediateParent%s", evaluator);
        }
    }

    static class PreviousSibling extends StructuralEvaluator {
        public PreviousSibling(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(HtmlElement root, HtmlElement element) {
            if (root == element)
                return false;

            HtmlElement prev = element.previousElementSibling();

            while (prev != null) {
                if (evaluator.matches(root, prev))
                    return true;

                prev = prev.previousElementSibling();
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(":prev*%s", evaluator);
        }
    }

    static class ImmediatePreviousSibling extends StructuralEvaluator {
        public ImmediatePreviousSibling(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(HtmlElement root, HtmlElement element) {
            if (root == element)
                return false;

            HtmlElement prev = element.previousElementSibling();
            return prev != null && evaluator.matches(root, prev);
        }

        @Override
        public String toString() {
            return String.format(":prev%s", evaluator);
        }
    }
}
