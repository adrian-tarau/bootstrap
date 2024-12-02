package net.microfalx.bootstrap.content;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;

/**
 * A fragment of text, usually used in an editor.
 */
@ToString
public class Fragment extends NamedAndTaggedIdentifyAware<String> {

    private Type type;
    private Language language;

    private String group;
    private String path;
    private String abbreviation;

    /**
     * Returns the type of the fragment.
     *
     * @return a non-null instance
     */
    public Type getType() {
        return type;
    }

    /**
     * The language in which the fragment is written.
     *
     * @return a non-null instance
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Returns the class path which holds the content of the fragment.
     * <p>
     * The path is relative to ~/content directory under resources.
     *
     * @return a non-null instance
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the abbreviation (keyword) used by editors to insert a snippet (followed by pressing tab).
     *
     * @return the abbreviation, null if it does not have one
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Returns the resource which holds the content of the fragment.
     *
     * @return a non-null instance
     * @see #getPath()
     */
    public Resource getResource() {
        return ClassPathResource.file("content/" + StringUtils.removeStartSlash(path));
    }

    /**
     * The language in which the fragment is written
     */
    public enum Language {
        NONE,
        JAVASCRIPT,
        JAVA,
        SQL
    }

    public enum Type {
        SNIPPET,
        EXAMPLE
    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private Type type = Type.EXAMPLE;
        private Language language = Language.NONE;

        private String group;
        private String path;
        private String abbreviation;

        public Builder(String id) {
            super(id);
        }

        public Builder type(Type type) {
            requireNonNull(type);
            this.type = type;
            return this;
        }

        public Builder language(Language language) {
            requireNonNull(language);
            this.language = language;
            return this;
        }

        public Builder group(String group) {
            requireNonNull(group);
            this.group = group;
            return this;
        }

        public Builder path(String path) {
            requireNonNull(path);
            this.path = path;
            return this;
        }

        public Builder abbreviation(String abbreviation) {
            this.abbreviation = abbreviation;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Fragment();
        }

        @Override
        public Fragment build() {
            if (isEmpty(group)) throw new IllegalArgumentException("The fragment group is required");
            if (isEmpty(path)) throw new IllegalArgumentException("The fragment resource is required");
            Fragment fragment = (Fragment) super.build();
            fragment.type = type;
            fragment.language = language;
            fragment.group = group;
            fragment.path = path;
            fragment.abbreviation = abbreviation;
            return fragment;
        }
    }
}
