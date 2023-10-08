package net.microfalx.bootstrap.search;

import net.microfalx.lang.Nameable;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds statistics for a term.
 */
public class TermStatistics implements Nameable {

    private final String field;
    private final String name;

    long frequency;
    long count;

    TermStatistics(String field, String name) {
        requireNonNull(field);
        requireNonNull(name);
        this.field = field;
        this.name = name;
    }

    /**
     * Returns the field
     *
     * @return a non-null instance
     */
    public String getField() {
        return field;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the total number of occurrences of term across all documents (the sum of the freq() for each doc that has this term).
     *
     * @return a positive integer if it can be provided, -1 if it is not supported for this term
     */
    public long getFrequency() {
        return frequency;
    }

    /**
     * Returns the number of documents containing the term.
     *
     * @return a positive integer
     */
    public long getCount() {
        return count;
    }
}
