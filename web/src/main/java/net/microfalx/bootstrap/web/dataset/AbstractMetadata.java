package net.microfalx.bootstrap.web.dataset;

import net.microfalx.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for all metadata.
 *
 * @param <M> the model type
 */
public abstract class AbstractMetadata<M> implements Metadata<M> {

    private final String id;
    private String name;
    private String description;
    private final Class<M> modelClass;
    private final Map<String, Field<M>> fields = new HashMap<>();

    public AbstractMetadata(Class<M> modelClass) {
        this.modelClass = modelClass;
        this.id = modelClass.getName();
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
    public final Collection<Field<?>> getFields() {
        return Collections.unmodifiableCollection(fields.values());
    }

    @Override
    public Field<?> find(String nameOrProperty) {
        requireNonNull(nameOrProperty);
        return fields.get(StringUtils.toIdentifier(nameOrProperty));
    }

    @Override
    public Field<?> get(String nameOrProperty) {
        Field<?> field = find(nameOrProperty);
        if (field == null) {
            throw new DataSetException("A field with name or property '" + nameOrProperty + "' is not registered in " + getName());
        }
        return field;
    }

    void addField(Field<M> field) {
        requireNonNull(field);
        fields.put(field.getId(), field);
        fields.put(StringUtils.toIdentifier(field.getName()), field);
        fields.put(StringUtils.toIdentifier(field.getProperty()), field);
    }

    @Override
    public String toString() {
        return "AbstractMetadata{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", modelClass=" + modelClass +
                ", fields=" + fields +
                '}';
    }
}
