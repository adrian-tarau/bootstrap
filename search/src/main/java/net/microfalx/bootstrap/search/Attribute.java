package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.model.AbstractAttribute;

import java.io.Serial;
import java.io.Serializable;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds a custom attribute for a document.
 * <p>
 * By default an attribute is only stored. If the attribute needs to be indexed (and possible tokenized),
 * use {@link #setIndexed(boolean)} and {@link #setTokenized(boolean)}.
 */
public class Attribute extends AbstractAttribute implements Serializable {

    @Serial
    private static final long serialVersionUID = 8394382351374244394L;

    static final int INDEXED_MASK = 0x01;
    static final int TOKENIZED_MASK = 0x02;
    static final int STORED_MASK = 0x04;

    private int options = STORED_MASK;

    /**
     * Creates an attribute instance from a generic attribute.
     *
     * @param attribute the attribute instance
     * @return a non-null instance
     */
    public static Attribute create(net.microfalx.bootstrap.model.Attribute attribute) {
        requireNonNull(attribute);
        return new Attribute(attribute.getName(), attribute.getValue());
    }

    /**
     * Creates an attribute instance.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return a non-null instance
     */
    public static Attribute create(String name, Object value) {
        return new Attribute(name, value);
    }

    private Attribute(String name, Object value) {
        super(name, value);
    }

    /**
     * Returns whether the attribute is tokenized.
     *
     * @return <code>true</code> if tokenized, <code>false</code> otherwise
     */
    public boolean isTokenized() {
        return (options & TOKENIZED_MASK) != 0;
    }

    /**
     * Changes whether the attribute is tokenized.
     *
     * @param tokenized <code>true</code> if tokenized, <code>false</code> otherwise
     * @return self
     */
    public Attribute setTokenized(boolean tokenized) {
        if (tokenized) {
            options |= TOKENIZED_MASK;
        } else {
            options &= ~TOKENIZED_MASK;
        }
        if (tokenized) {
            // if tokenized, probably must be indexed too
            setIndexed(true);
        }
        return this;
    }

    /**
     * Returns whether the attribute is stored in the index.
     *
     * @return <code>true</code> if stored, <code>false</code> otherwise
     */
    public boolean isStored() {
        return (options & STORED_MASK) != 0;
    }

    /**
     * Changes whether the attribute is stored.
     *
     * @param stored <code>true</code> if stored, <code>false</code> otherwise
     * @return self
     */
    public Attribute setStored(boolean stored) {
        if (stored) {
            options |= STORED_MASK;
        } else {
            options &= ~STORED_MASK;
        }
        return this;
    }

    /**
     * Returns whether the attribute is indexed.
     *
     * @return <code>true</code> if indexed, <code>false</code> otherwise
     */
    public boolean isIndexed() {
        return (options & INDEXED_MASK) != 0;
    }

    /**
     * Changes whether the attribute is indexed.
     *
     * @param indexed <code>true</code> if indexed, <code>false</code> otherwise
     * @return self
     */
    public Attribute setIndexed(boolean indexed) {
        if (indexed) {
            options |= INDEXED_MASK;
        } else {
            options &= ~INDEXED_MASK;
        }
        return this;
    }

    /**
     * Returns the options associated with the attribute.
     *
     * @return the options
     */
    public int getOptions() {
        return options;
    }

    void setOptions(int options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name=" + getName() +
                ", value=" + getValue() +
                ", options=" + options +
                '}';
    }
}
