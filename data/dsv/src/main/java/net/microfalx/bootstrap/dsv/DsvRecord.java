package net.microfalx.bootstrap.dsv;

import org.apache.commons.csv.CSVRecord;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Represents a record in a Delimiter-Separated Values (DSV).
 * <p>
 * This class wraps an Apache Commons CSVRecord to provide additional functionality if needed.
 */
public class DsvRecord {

    private final DsvMetadata metadata;
    private final CSVRecord record;

    public DsvRecord(DsvMetadata metadata, CSVRecord record) {
        requireNonNull(metadata);
        requireNonNull(record);
        this.metadata = metadata;
        this.record = record;
    }

    /**
     * Returns the metadata associated with this DSV record.
     *
     * @return a non-null instance
     */
    public DsvMetadata getMetadata() {
        return metadata;
    }

    /**
     * Returns the record value at the specified field index.
     *
     * @param index the index of the value to retrieve
     * @return the value
     */
    public String get(int index) {
        return record.get(index);
    }

    /**
     * Returns the record value for a specified field name.
     *
     * @param name the name of the field to retrieve
     * @return the value
     */
    public String get(String name) {
        DsvField field = metadata.find(name);
        return field != null ? field.get(this, String.class) : null;
    }

    /**
     * Changes the value at the specified field index.
     *
     * @param index the index of the value to change
     * @param value the value
     */
    public DsvRecord set(int index, String value) {
        record.values()[index] = value;
        return this;
    }

    /**
     * Changes the value at the specified field name.
     *
     * @param name  the name of the field to set
     * @param value the value
     */
    public DsvRecord set(String name, String value) {
        DsvField field = metadata.find(name);
        if (field != null) field.set(this, value);
        return this;
    }
}
