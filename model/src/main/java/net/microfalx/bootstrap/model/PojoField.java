package net.microfalx.bootstrap.model;

import jodd.typeconverter.TypeConverterManager;
import net.microfalx.lang.Id;
import net.microfalx.lang.ReadOnly;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static org.apache.commons.lang3.ClassUtils.isAssignable;

public abstract class PojoField<M> extends AbstractField<M> {

    private static final TypeConverterManager TYPE_CONVERTER_MANAGER = TypeConverterManager.get();

    private MethodHandle setter;
    private MethodHandle getter;
    private Collection<? extends Annotation> annotations = Collections.emptyList();

    public PojoField(PojoMetadata<M, PojoField<M>> metadata, String name, String property) {
        super(metadata, name, property);
    }

    @Override
    public Object get(M model) {
        if (getter == null) throw new ModelException("The field '" + getName() + "' cannot be read");
        try {
            return getter.invokeExact(model);
        } catch (Throwable e) {
            throw new ModelException("Failed to extract field '" + getName() + "' value", e);
        }
    }

    @Override
    public void set(M model, Object value) {
        if (setter == null) throw new ModelException("The field '" + getName() + "' is read only");
        try {
            getter.invokeExact(model, value);
        } catch (Throwable e) {
            throw new ModelException("Failed to extract field '" + getName() + "' value", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A findAnnotation(Class<A> annotationClass) {
        requireNonNull(annotationClass);
        for (Annotation annotation : annotations) {
            if (isAssignable(annotation.getClass(), annotationClass)) return (A) annotation;
        }
        return null;
    }

    void update(MethodHandle getter, MethodHandle setter) {
        this.getter = getter;
        this.setter = setter;
        setReadOnly(setter == null || hasAnnotation(ReadOnly.class));
    }

    protected void update(Field field) {
        requireNonNull(field);
        annotations = Arrays.asList(field.getAnnotations());
        setId(hasAnnotation(Id.class));
    }
}
