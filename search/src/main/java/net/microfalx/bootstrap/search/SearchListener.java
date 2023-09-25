package net.microfalx.bootstrap.search;

/**
 * A listener picked by {@link SearchService}.
 */
public interface SearchListener {

    /**
     * Returns the label for a given field.
     * <p>
     * First listener which returns a non-empty label wins.
     *
     * @param field the field name
     * @return the label
     */
    default String getLabel(String field) {
        return null;
    }

    /**
     * Returns the description for a given field.
     * <p>
     * First listener which returns a non-empty label wins.
     *
     * @param field the field name
     * @return the label
     */
    default String getDescription(String field) {
        return null;
    }

    /**
     * Returns whether the attribute will be displayed in the search result.
     *
     * @param document  the search engine document
     * @param attribute the attribute
     * @return {@code true} if accepted, {@code false} otherwise
     */
    default boolean accept(Document document, Attribute attribute) {
        return true;
    }
}
