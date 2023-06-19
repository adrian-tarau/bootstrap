package net.microfalx.bootstrap.model;

import net.microfalx.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for all metadata.
 *
 * @param <M> the model type
 */
public abstract class AbstractMetadata<M, F extends Field<M>> implements Metadata<M, F> {

    private final String id;
    private String name;
    private String description;
    private final Class<M> modelClass;
    private final Map<String, F> fields = new HashMap<>();
    private F idField;
    private final Map<String, F> idFields = new HashMap<>();

    public AbstractMetadata(Class<M> modelClass) {
        this.modelClass = modelClass;
        this.id = modelClass.getName();
        this.name = org.apache.commons.lang3.StringUtils.capitalize(modelClass.getSimpleName());
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getName() {
        return name;
    }

    protected final void setName(String name) {
        requireNotEmpty(name);
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    protected final void setDescription(String description) {
        this.description = description;
    }

    @Override
    public final Class<M> getModel() {
        return modelClass;
    }

    @Override
    public final Collection<F> getFields() {
        return unmodifiableCollection(fields.values());
    }

    @Override
    public Collection<F> getIdFields() {
        return unmodifiableCollection(idFields.values());
    }

    @Override
    public F findIdField() {
        if (idFields.size() > 1) throw new ModelException("Multiple identifier fields are present for " + getName());
        return idField != null ? idField : null;
    }

    @Override
    public F find(String nameOrProperty) {
        requireNonNull(nameOrProperty);
        return fields.get(StringUtils.toIdentifier(nameOrProperty));
    }

    @SuppressWarnings("unchecked")
    @Override
    public F get(String nameOrProperty) {
        Field<?> field = find(nameOrProperty);
        if (field == null) {
            throw new FieldNotFoundException("A field with name or property '" + nameOrProperty + "' is not registered in " + getName());
        }
        return (F) field;
    }

    @Override
    public <A extends Annotation> A findAnnotation(Class<A> annotationClass) {
        return modelClass.getAnnotation(annotationClass);
    }

    @Override
    public <A extends Annotation> boolean hasAnnotation(Class<A> annotationClass) {
        return findAnnotation(annotationClass) != null;
    }

    /**
     * Adds a new field to the metadata.
     *
     * @param field the field
     */
    public void addField(F field) {
        requireNonNull(field);
        fields.put(field.getId(), field);
        fields.put(StringUtils.toIdentifier(field.getName()), field);
        fields.put(StringUtils.toIdentifier(field.getProperty()), field);

        if (field.isId()) {
            idField = field;
            idFields.put(field.getId(), field);
            idFields.put(StringUtils.toIdentifier(field.getName()), field);
            idFields.put(StringUtils.toIdentifier(field.getProperty()), field);
        }
        if (idFields.size() > 1) idField = null;
    }

    @Override
    public String toString() {
        return "AbstractMetadata{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", modelClass=" + modelClass +
                ", fields=" + fields.size() +
                '}';
    }
}
