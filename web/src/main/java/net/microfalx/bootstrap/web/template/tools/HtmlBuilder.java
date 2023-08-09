package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.lang.TextUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

public class HtmlBuilder {

    private final static int DEFAULT_INDENT = 4;
    private final static String ROOT_ELEMENT = "#ROOT";

    private final Element root;
    private int position;
    private int indent;
    private int lineCount;

    public HtmlBuilder() {
        root = new Element(ROOT_ELEMENT, false);
    }

    public HtmlBuilder(String name) {
        root = new Element(name, false);
    }

    public int getPosition() {
        return position;
    }

    public HtmlBuilder setPosition(int position) {
        this.position = position;
        return this;
    }

    public int getIndent() {
        return indent;
    }

    public HtmlBuilder setIndent(int indent) {
        this.indent = indent;
        return this;
    }

    private void update(StringBuilder builder, Fragment fragment, int position) {
        if (fragment instanceof Element) {
            if (builder.length() > 0)
                for (Fragment childFragment : ((Element) fragment).fragments) {

                }
        } else if (fragment instanceof Text) {
            builder.append(TextUtils.insertSpaces(((Text) fragment).value, position));
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        update(builder, root, position);
        return builder.toString();
    }

    public static abstract class Fragment {

    }

    public static class Text extends Fragment {

        private String value;

        Text(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Text{" +
                    "value='" + value + '\'' +
                    "} " + super.toString();
        }
    }

    public static class Element extends Fragment {

        private final String name;
        private final boolean inline;
        private final List<Attribute> attributes = new ArrayList<>();
        private final List<Fragment> fragments = new ArrayList<>();

        Element(String name, boolean inline) {
            requireNotEmpty(name);
            this.name = name;
            this.inline = inline;
        }

        public String getName() {
            return name;
        }

        public boolean isInline() {
            return inline;
        }

        public List<Attribute> getAttributes() {
            return unmodifiableList(attributes);
        }

        public List<Fragment> getFragments() {
            return unmodifiableList(fragments);
        }

        public Element attribute(String name) {
            return attribute(name, null);
        }

        public Element attribute(String name, Object value) {
            attributes.add(new Attribute(name, null));
            return this;
        }

        public Element div() {
            return element("div", false);
        }

        public Element span() {
            return element("span", true);
        }

        public Element element(String name) {
            return element(name, true);
        }

        public Element element(String name, boolean inline) {
            Element element = new Element(name, inline);
            fragments.add(element);
            return element;
        }

        public Element text(String value) {
            fragments.add(new Text(value));
            return this;
        }

        @Override
        public String toString() {
            return "Element{" +
                    "name='" + name + '\'' +
                    ", attributes=" + attributes +
                    ", fragments=" + fragments +
                    "} " + super.toString();
        }
    }

    public static class Attribute {

        private final String name;
        private final Object value;

        Attribute(String name, Object value) {
            requireNotEmpty(name);
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Attribute{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
