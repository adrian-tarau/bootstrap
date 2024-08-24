package net.microfalx.bootstrap.model;

public class EnumField<E extends Enum<E>> extends AbstractField<E> {

    public EnumField(AbstractMetadata<E, ? extends AbstractField<E>, ?> metadata, String name, String property) {
        super(metadata, name, property);
    }

    @Override
    public Object get(E model) {
        switch (getIndex()) {
            case 0:
                return model.name();
            case 1:
                return getDisplay(model);
            default:
                return throwInvalidField(getIndex());
        }
    }

    @Override
    public <V> V get(E model, Class<V> type) {
        Object value = get(model);
        return Field.from(value, type);
    }

    @Override
    public void set(E model, Object value) {
        throw new ModelException("Enum fields are immutable");
    }

    @Override
    public String getDisplay(E model) {
        return getI18n().getText(model);
    }

    private <T> T throwInvalidField(int index) {
        throw new FieldNotFoundException("Field with index " + index + " does not exist");
    }
}
