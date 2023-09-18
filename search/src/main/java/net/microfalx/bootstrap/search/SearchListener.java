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
}
