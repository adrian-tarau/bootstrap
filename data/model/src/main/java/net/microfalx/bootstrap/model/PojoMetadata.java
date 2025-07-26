package net.microfalx.bootstrap.model;

import net.microfalx.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import static net.microfalx.lang.ClassUtils.isSubClassOf;

/**
 * Base class for all POJO metadata.
 *
 * @param <M> the model
 */
public abstract class PojoMetadata<M, F extends PojoField<M>, ID> extends AbstractMetadata<M, F, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PojoMetadata.class);

    private static final String CLASS_FIELD_NAME = "class";

    @SuppressWarnings("unchecked")
    public PojoMetadata(Class<M> modelClass) {
        super(modelClass);
        extractFields((PojoMetadata<M, PojoField<M>, ID>) this, modelClass);
    }

    /**
     * Returns whether the property should be used.
     *
     * @param propertyDescriptor the property descriptor
     * @return {@code true} to accept the field, {@code false} otherwise
     */
    protected boolean accept(PropertyDescriptor propertyDescriptor) {
        return true;
    }

    /**
     * Creates a field implementation
     *
     * @param metadata the metadata
     * @param name     the field name
     * @param property the property name
     * @return a non-null instance
     */
    protected abstract F createField(PojoMetadata<M, PojoField<M>, ID> metadata, String name, String property);

    /**
     * Extracts Java Bean metadata and create fields as needed.
     *
     * @param metadata   the metadata instance
     * @param modelClass the model class
     */
    private void extractFields(PojoMetadata<M, PojoField<M>, ID> metadata, Class<?> modelClass) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(modelClass);
        int index = 0;
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (!accept(propertyDescriptor) || CLASS_FIELD_NAME.equals(propertyDescriptor.getName())) continue;
            try {
                PojoField<M> field = createField(metadata, propertyDescriptor.getName(), propertyDescriptor.getName());
                field.setDataClass(propertyDescriptor.getPropertyType());
                Class<?> genericDataClass = getGenericDataClass(propertyDescriptor);
                if (genericDataClass != null) field.setGenericDataClass(genericDataClass);
                field.setIndex(index++);
                field.update(getGetter(propertyDescriptor), getSetter(propertyDescriptor));
                Field jvmField = ReflectionUtils.findField(modelClass, propertyDescriptor.getName());
                boolean accepted = false;
                if (jvmField != null) {
                    accepted = field.update(jvmField);
                } else if (propertyDescriptor.getReadMethod() != null) {
                    accepted = field.update(propertyDescriptor.getReadMethod());
                }
                if (accepted) metadata.addField(field);
            } catch (Exception e) {
                throw new ModelException("Failed to process field '" + propertyDescriptor.getName()
                        + "' in " + ClassUtils.getName(modelClass), e);
            }
        }
    }

    private MethodHandle getGetter(PropertyDescriptor propertyDescriptor) {
        Method method = propertyDescriptor.getReadMethod();
        try {
            return method != null ? MethodHandles.lookup().unreflect(method) : null;
        } catch (IllegalAccessException e) {
            throw new ModelException("Failed to extract getter for " + propertyDescriptor, e);
        }
    }

    private MethodHandle getSetter(PropertyDescriptor propertyDescriptor) {
        Method method = propertyDescriptor.getWriteMethod();
        try {
            return method != null ? MethodHandles.lookup().unreflect(method) : null;
        } catch (IllegalAccessException e) {
            throw new ModelException("Failed to extract setter for " + propertyDescriptor, e);
        }
    }

    private Class<?> getGenericDataClass(PropertyDescriptor propertyDescriptor) {
        Method method = propertyDescriptor.getReadMethod();
        Class<?> propertyType = propertyDescriptor.getPropertyType();
        boolean isCollection = isSubClassOf(propertyType, Collection.class);
        boolean isMap = isSubClassOf(propertyType, Map.class);
        if (method == null || !(isCollection || isMap)) return null;
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type type = parameterizedType.getActualTypeArguments()[isCollection ? 0 : 1];
            return type instanceof Class ? (Class<?>) type : null;
        } else {
            return null;
        }
    }
}
