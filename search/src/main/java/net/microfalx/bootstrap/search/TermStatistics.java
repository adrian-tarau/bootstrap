package net.microfalx.bootstrap.search;

import net.microfalx.lang.Nameable;

import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds statistics for a term.
 */
public class TermStatistics implements Nameable {

    private final String field;
    private final String value;

    long frequency;
    long count;

    TermStatistics(String field, String value) {
        requireNonNull(field);
        requireNonNull(value);
        this.field = field;
        this.value = value;
    }

    /**
     * Returns the field name.
     *
     * @return a non-null instance
     */
    public String getField() {
        return field;
    }

    /**
     * Returns the value of the field.
     *
     * @return a non-null instance
     */
    public String getValue() {
        return value;
    }

    @Override
    public String getName() {
        return value;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TermStatistics that = (TermStatistics) o;
        return Objects.equals(field, that.field) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value);
    }

    @Override
    public String toString() {
        return "TermStatistics{" +
                "field='" + field + '\'' +
                ", value='" + value + '\'' +
                ", frequency=" + frequency +
                ", count=" + count +
                '}';
    }
}
