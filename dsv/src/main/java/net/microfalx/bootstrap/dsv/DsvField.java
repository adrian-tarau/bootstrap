package net.microfalx.bootstrap.dsv;

import net.microfalx.bootstrap.model.AbstractField;
import net.microfalx.bootstrap.model.AbstractMetadata;
import net.microfalx.bootstrap.model.Field;

/**
 * A model field for a DSV (Delimiter-Separated Values) record.
 */
public class DsvField extends AbstractField<DsvRecord> {

    public DsvField(AbstractMetadata<DsvRecord, DsvField, String> metadata, String name, String property) {
        super(metadata, name, property);
    }

    @Override
    public Object get(DsvRecord model) {
        return model.get(getIndex());
    }

    @Override
    public <V> V get(DsvRecord model, Class<V> type) {
        Object value = get(model);
        return Field.from(value, type);
    }

    @Override
    public void set(DsvRecord model, Object value) {
        model.set(getIndex(), Field.from(value, String.class));
    }

    @Override
    public String getDisplay(DsvRecord model) {
        return Field.from(get(model), String.class);
    }
}
