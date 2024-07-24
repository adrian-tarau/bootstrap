package net.microfalx.bootstrap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.lang.reflect.Member;

public final class JpaField<M> extends PojoField<M> {

    private String columnName;

    public JpaField(PojoMetadata<M, PojoField<M>, ?> metadata, String name, String property) {
        super(metadata, name, property);
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    protected boolean update(Member member) {
        boolean accepted = super.update(member);
        setId(hasAnnotation(Id.class));
        Column columnAnnot = findAnnotation(Column.class);
        if (columnAnnot != null) {
            columnName = columnAnnot.name();
            setRequired(!columnAnnot.nullable());
        }
        setTransient(hasAnnotation(Transient.class));
        return accepted;
    }
}
