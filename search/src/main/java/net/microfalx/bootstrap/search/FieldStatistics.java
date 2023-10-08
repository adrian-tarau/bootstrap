package net.microfalx.bootstrap.search;

import net.microfalx.lang.Nameable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Comparator.comparingLong;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds statistics for a field.
 */
public class FieldStatistics implements Nameable {

    private final String name;
    long documentCount;
    int termCount;
    private List<TermStatistics> terms = new ArrayList<>();

    FieldStatistics(String name) {
        requireNonNull(name);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the number of documents which have this field.
     *
     * @return a positive integer
     */
    public long getDocumentCount() {
        return documentCount;
    }

    /**
     * Returns the number of terms stored in this field.
     *
     * @return a positiver integer
     */
    public int getTermCount() {
        return termCount;
    }

    /**
     * Returns the top terms for this field.
     *
     * @return a non-null instance
     */
    public List<TermStatistics> getTerms() {
        return Collections.unmodifiableList(terms);
    }

    /**
     * Updates the list of terms.
     *
     * @param terms the terms
     */
    void setTerms(List<TermStatistics> terms) {
        this.terms = new ArrayList<>(terms);
        this.terms.sort(comparingLong(TermStatistics::getCount).reversed());
    }

    @Override
    public String toString() {
        return "FieldStatistics{" +
                "name='" + name + '\'' +
                "documentCount='" + documentCount + '\'' +
                '}';
    }
}
