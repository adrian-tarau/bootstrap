package net.microfalx.bootstrap.model;

public class EnumMetadata<E extends Enum<E>> extends AbstractMetadata<E, EnumField<E>, String> {

    public EnumMetadata(Class<E> modelClass) {
        super(modelClass);
        initFields();
    }

    private void initFields() {
        EnumField<E> idField = new EnumField<>(this, "id", null);
        idField.setReadOnly(true);
        idField.setId(true);
        idField.setNaturalId(true);
        addField(idField);

        EnumField<E> nameField = new EnumField<>(this, "name", null);
        nameField.setReadOnly(true);
        nameField.setIsName(true);
        addField(nameField);
    }
}
