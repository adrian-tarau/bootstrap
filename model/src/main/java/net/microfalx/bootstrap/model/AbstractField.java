package net.microfalx.bootstrap.model;

import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.I18n;
import net.microfalx.lang.annotation.Label;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.*;
import static org.apache.commons.lang3.ClassUtils.isAssignable;

/**
 * Base class for all fields.
 *
 * @param <M> the model type
 */
public abstract class AbstractField<M> implements Field<M> {

    private final AbstractMetadata<M, ? extends AbstractField<M>, ?> metadata;
    private final String id;
    private final String name;
    private final String property;
    private boolean isId;
    private boolean isName;
    private int index;
    private int position;
    private Class<?> dataClass = Object.class;
    private DataType dataType = DataType.MODEL;
    private boolean readOnly;
    private boolean _transient;

    public AbstractField(AbstractMetadata<M, ? extends AbstractField<M>, ?> metadata, String name, String property) {
        requireNotEmpty(metadata);
        requireNotEmpty(name);
        requireNotEmpty(property);

        this.id = toIdentifier(name);
        this.metadata = metadata;
        this.name = name;
        this.property = property;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final Metadata<M, ? extends Field<M>, ?> getMetadata() {
        return metadata;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getLabel() {
        String label = metadata.getI18n(getI18nPrefix() + ".label");
        if (StringUtils.isEmpty(label)) {
            Label labelAnnot = findAnnotation(Label.class);
            if (labelAnnot != null) {
                return isNotEmpty(labelAnnot.icon()) && isEmpty(labelAnnot.value()) ? EMPTY_STRING : labelAnnot.value();
            }
        }
        return isNotEmpty(label) ? label : StringUtils.beautifyCamelCase(getName());
    }

    @Override
    public String getLabelIcon() {
        Label labelAnnot = findAnnotation(Label.class);
        return labelAnnot != null && isNotEmpty(labelAnnot.icon()) ? labelAnnot.icon() : null;
    }

    @Override
    public String getDescription() {
        return metadata.getI18n(getI18nPrefix() + ".description");
    }

    @Override
    public final String getProperty() {
        return property;
    }

    @Override
    public boolean isId() {
        return isId;
    }

    protected final void setIsId(boolean isId) {
        this.isId = isId;
    }

    @Override
    public final boolean isName() {
        return isName;
    }

    protected final void setIsName(boolean isName) {
        this.isName = isName;
    }

    @Override
    public final int getIndex() {
        return index;
    }

    protected final void setIndex(int index) {
        this.index = index;
    }

    @Override
    public final int getPosition() {
        return position;
    }

    protected void setPosition(int position) {
        this.position = position;
    }

    public final boolean isReadOnly() {
        return readOnly;
    }

    protected final void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public final boolean isTransient() {
        return _transient;
    }

    protected final void setTransient(boolean _transient) {
        this._transient = _transient;
    }

    @Override
    public final Class<?> getDataClass() {
        return dataClass;
    }

    final void setDataClass(Class<?> dataClass) {
        requireNonNull(dataClass);
        this.dataClass = dataClass;
        updateDataType(dataClass);
    }

    @Override
    public final DataType getDataType() {
        return dataType;
    }

    @Override
    public <A extends Annotation> A findAnnotation(Class<A> annotationClass) {
        return null;
    }

    @Override
    public final <A extends Annotation> boolean hasAnnotation(Class<A> annotationClass) {
        return findAnnotation(annotationClass) != null;
    }

    /**
     * Converts an object to a target type.
     *
     * @param value  the value to convert
     * @param target the target class
     * @param <T>    the type of the target value
     * @return the converted value
     */
    protected final <T> T from(Object value, Class<T> target) {
        try {
            return Converters.from(value, target);
        } catch (Exception e) {
            throw new InvalidDataTypeExpression("Data conversion failure for field '" + getName() + "' from object '"
                    + value + "' to type '" + net.microfalx.lang.ClassUtils.getName(target) + "'", e);
        }
    }

    private void updateDataType(Class<?> dataClass) {
        DataType dataType = dataTypes.get(dataClass);
        if (dataType != null) {
            this.dataType = dataType;
        } else if (isAssignable(dataClass, Enum.class)) {
            this.dataType = DataType.ENUM;
        } else if (isAssignable(dataClass, Collection.class) || isAssignable(dataClass, Map.class)) {
            this.dataType = DataType.COLLECTION;
        } else {
            this.dataType = DataType.MODEL;
        }
    }

    private String getI18nPrefix() {
        I18n i18nAnnot = findAnnotation(I18n.class);
        String suffix = toIdentifier(i18nAnnot != null ? i18nAnnot.value() : getName());
        return metadata.getI18nPrefix() + suffix;
    }

    @Override
    public String toString() {
        return "AbstractField{" +
                "metadata=" + metadata.getName() +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", property='" + property + '\'' +
                ", dataClass=" + dataClass +
                ", dataType=" + dataType +
                '}';
    }

    private static final Map<Class<?>, DataType> dataTypes = new HashMap<>();

    static {

        // numbers
        dataTypes.put(byte.class, DataType.INTEGER);
        dataTypes.put(Byte.class, DataType.INTEGER);
        dataTypes.put(short.class, DataType.INTEGER);
        dataTypes.put(Short.class, DataType.INTEGER);
        dataTypes.put(int.class, DataType.INTEGER);
        dataTypes.put(Integer.class, DataType.INTEGER);
        dataTypes.put(long.class, DataType.INTEGER);
        dataTypes.put(Long.class, DataType.INTEGER);
        dataTypes.put(BigInteger.class, DataType.INTEGER);
        dataTypes.put(float.class, DataType.NUMBER);
        dataTypes.put(Float.class, DataType.NUMBER);
        dataTypes.put(double.class, DataType.NUMBER);
        dataTypes.put(Double.class, DataType.NUMBER);
        dataTypes.put(BigDecimal.class, DataType.NUMBER);

        // date/time
        dataTypes.put(Date.class, DataType.DATE_TIME);
        dataTypes.put(java.sql.Date.class, DataType.DATE);
        dataTypes.put(java.sql.Time.class, DataType.TIME);
        dataTypes.put(LocalDate.class, DataType.DATE);
        dataTypes.put(LocalTime.class, DataType.TIME);
        dataTypes.put(LocalDateTime.class, DataType.DATE_TIME);
        dataTypes.put(ZonedDateTime.class, DataType.DATE_TIME);
        dataTypes.put(OffsetDateTime.class, DataType.DATE_TIME);

        // various
        dataTypes.put(boolean.class, DataType.BOOLEAN);
        dataTypes.put(Boolean.class, DataType.BOOLEAN);
        dataTypes.put(String.class, DataType.STRING);

    }
}
