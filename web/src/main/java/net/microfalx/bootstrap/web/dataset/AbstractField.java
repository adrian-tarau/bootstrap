package net.microfalx.bootstrap.web.dataset;

import net.microfalx.lang.StringUtils;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for all fields.
 *
 * @param <M> the model type
 */
public abstract class AbstractField<M> implements Field<M> {

    private final Metadata<M> metadata;
    private final String id;
    private final String name;
    private final String property;
    private boolean isId;
    private int index;
    private Class<?> dataClass = Object.class;
    private DataType dataType = DataType.MODEL;

    public AbstractField(Metadata<M> metadata, String name, String property) {
        requireNotEmpty(metadata);
        requireNotEmpty(name);
        requireNotEmpty(property);

        this.id = StringUtils.toIdentifier(name);
        this.metadata = metadata;
        this.name = name;
        this.property = property;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final Metadata<M> getMetadata() {
        return metadata;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getProperty() {
        return property;
    }

    @Override
    public boolean isId() {
        return isId;
    }

    protected void setId(boolean id) {
        this.isId = id;
    }

    @Override
    public final int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    @Override
    public final Class<?> getDataClass() {
        return dataClass;
    }

    void setDataClass(Class<?> dataClass) {
        requireNonNull(dataClass);
        this.dataClass = dataClass;
    }

    @Override
    public final DataType getDataType() {
        return dataType;
    }


    @Override
    public String toString() {
        return "AbstractField{" +
                "metadata=" + metadata +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", property='" + property + '\'' +
                ", dataClass=" + dataClass +
                ", dataType=" + dataType +
                '}';
    }
}
