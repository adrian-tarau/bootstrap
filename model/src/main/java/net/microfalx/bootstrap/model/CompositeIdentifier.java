package net.microfalx.bootstrap.model;

import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;

import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.append;

/**
 * A class which manages a composite identifier.
 * <p>
 * When multiple identify fields are registered for a model, the composite identifier is created out of the
 * individual field values separated by a {@code |}.
 *
 * @param <M> the model type
 */
public class CompositeIdentifier<M, F extends Field<M>> {

    public static final char SEPARATOR = '|';

    private final Metadata<M, F> metadata;
    private final M model;
    private final String id;
    private Object[] values;

    public CompositeIdentifier(Metadata<M, F> metadata, M model) {
        requireNonNull(metadata);
        requireNonNull(model);
        this.metadata = metadata;
        this.model = model;
        this.id = encodeId();
    }

    public CompositeIdentifier(Metadata<M, F> metadata, String id) {
        requireNonNull(metadata);
        requireNonNull(id);
        this.metadata = metadata;
        this.model = null;
        this.id = id;
        decodeId();
    }

    /**
     * Returns the values (identifiers) behind the composite identifier
     *
     * @return a non-null instance
     */
    public Object[] getValues() {
        return values;
    }

    private String encodeId() {
        List<F> idFields = getIdFields();
        if (idFields.size() == 1) {
            Object part = idFields.get(0).get(model);
            values = new Object[]{part};
            return ObjectUtils.toString(part);
        } else {
            values = new Object[idFields.size()];
            StringBuilder builder = new StringBuilder();
            int index = 0;
            for (F idField : idFields) {
                Object part = idField.get(model);
                values[index++] = part;
                append(builder, part, SEPARATOR);
            }
            return builder.toString();
        }
    }

    private void decodeId() {
        List<F> idFields = getIdFields();
        if (idFields.size() == 1) {
            F field = idFields.get(0);
            Object part = Field.from(id, field.getDataClass());
            values = new Object[]{part};
        } else {
            values = new Object[idFields.size()];
            String[] parts = StringUtils.split(id, String.valueOf(SEPARATOR));
            int index = 0;
            for (F idField : idFields) {
                Object part = Field.from(parts[index], idField.getDataClass());
                values[index++] = part;
            }
        }
    }

    private List<F> getIdFields() {
        List<F> idFields = metadata.getIdFields();
        if (idFields.isEmpty())
            throw new ModelException("Model '" + metadata.getName() + " does not have any identifiers");
        return idFields;
    }

    @Override
    public String toString() {
        return id;
    }
}
