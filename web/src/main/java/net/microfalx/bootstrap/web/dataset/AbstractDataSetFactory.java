package net.microfalx.bootstrap.web.dataset;

import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all data set factories.
 *
 * @param <M> the model type
 */
public abstract class AbstractDataSetFactory<M, ID> implements DataSetFactory<M, ID> {

    private final Map<Class<?>, Metadata<M>> metadataCache = new ConcurrentHashMap<>();

    @Override
    public Metadata<M> getMetadata(Class<M> modelClass) {
        requireNonNull(modelClass);
        Metadata<M> metadata = metadataCache.get(modelClass);
        if (metadata == null) {
            metadata = createMetadata(modelClass);
            extractFields((AbstractMetadata<M>) metadata, modelClass);
            metadataCache.put(modelClass, metadata);
        }
        return metadata;
    }

    @Override
    public void update(DataSet<M, ID> dataSet, Object owner) {
        // empty by default
    }

    /**
     * Subclasses would create the actual field.
     *
     * @param metadata the metadata
     * @param name     the field name
     * @param property the property name
     * @return a non-null instance
     */
    protected abstract AbstractField<M> createField(Metadata<M> metadata, String name, String property);

    /**
     * Subclasses would create the actual metadata.
     *
     * @param modelClass the model type
     * @return a non-null instance
     */
    protected abstract AbstractMetadata<M> createMetadata(Class<M> modelClass);

    private void extractFields(AbstractMetadata<M> metadata, Class<?> modelClass) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(modelClass);
        int index = 0;
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            AbstractField<M> field = createField(metadata, propertyDescriptor.getName(), propertyDescriptor.getName());
            field.setDataClass(propertyDescriptor.getPropertyType());
            field.setIndex(index++);
            metadata.addField(field);
        }
    }
}
