package net.microfalx.bootstrap.model;

import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static org.apache.commons.lang3.ClassUtils.isAssignable;

public abstract class PojoField<M> extends AbstractField<M> {

    private MethodHandle setter;
    private MethodHandle getter;
    private Collection<? extends Annotation> annotations = Collections.emptyList();

    public PojoField(PojoMetadata<M, PojoField<M>, ?> metadata, String name, String property) {
        super(metadata, name, property);
    }

    @Override
    public Object get(M model) {
        if (getter == null) throw new ModelException("The field '" + getName() + "' cannot be read");
        try {
            return getter.invoke(model);
        } catch (Throwable e) {
            throw new ModelException("Failed to get field '" + getName() + "' value", e);
        }
    }

    @Override
    public void set(M model, Object value) {
        if (setter == null) throw new ModelException("The field '" + getName() + "' is read only");
        try {
            value = from(value, getDataClass());
            setter.invoke(model, value);
        } catch (Throwable e) {
            throw new ModelException("Failed to set field '" + getName() + "' value", e);
        }
    }

    @Override
    public String getDisplay(M model) {
        Object value = get(model);
        return ObjectUtils.toString(value);
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

    protected boolean update(Member member) {
        requireNonNull(member);
        if (((AnnotatedElement) member).isAnnotationPresent(Ignore.class)) return false;
        annotations = Arrays.asList(((AnnotatedElement) member).getAnnotations());
        Position positionAnnot = findAnnotation(Position.class);
        setPosition(positionAnnot != null ? positionAnnot.value() : 1 + getIndex() * 10);
        setIsId(hasAnnotation(Id.class));
        setIsName(hasAnnotation(Name.class));
        return true;
    }
}
