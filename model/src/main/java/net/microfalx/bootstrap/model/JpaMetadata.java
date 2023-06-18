package net.microfalx.bootstrap.model;

import jakarta.persistence.Table;

public final class JpaMetadata<M> extends PojoMetadata<M, JpaField<M>> {

    private String tableName;

    public JpaMetadata(Class<M> modelClass) {
        super(modelClass);
        update();
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    protected JpaField<M> createField(PojoMetadata<M, PojoField<M>> metadata, String name, String property) {
        return new JpaField<>(metadata, name, property);
    }

    private void update() {
        Table tableAnnotation = findAnnotation(Table.class);
        if (tableAnnotation != null) {
            tableName = tableAnnotation.name();
        }
    }
}
