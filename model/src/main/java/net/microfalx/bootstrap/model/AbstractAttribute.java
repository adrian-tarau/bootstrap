package net.microfalx.bootstrap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;

import static java.lang.Integer.compare;
import static net.microfalx.bootstrap.model.AttributeUtils.getAttributePriority;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.capitalizeWords;

/**
 * Base class for all attributes.
 */
public abstract class AbstractAttribute implements Attribute, Comparable<Attribute> {

    private final String name;
    private final Object value;
    private String label;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @JsonIgnore
    Attributes<? extends Attribute> parent;

    public AbstractAttribute(String name, Object value) {
        requireNonNull(name);
        this.name = name;
        this.value = value;
    }

    @Override
    public final Attributes<? extends Attribute> getParent() {
        return parent;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getLabel() {
        if (StringUtils.isEmpty(label)) label = capitalizeWords(getName());
        return label;
    }

    public final void setLabel(String label) {
        this.label = label;
    }

    @Override
    public final Object getValue() {
        return value;
    }

    @Override
    public final String asString() {
        return Field.from(value, String.class);
    }

    @Override
    public final boolean isEmpty() {
        return ObjectUtils.isEmpty(value);
    }

    @Override
    public final boolean isNull() {
        return value == null;
    }

    public final String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(Attribute o) {
        if (o == null) return 1;
        Integer thisPriority = getAttributePriority(getName());
        Integer thatPriority = getAttributePriority(o.getName());
        if (thisPriority != null && thatPriority != null) {
            return compare(thisPriority, thatPriority);
        } else {
            return getName().compareToIgnoreCase(o.getName());
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", parent=" + parent +
                '}';
    }
}
