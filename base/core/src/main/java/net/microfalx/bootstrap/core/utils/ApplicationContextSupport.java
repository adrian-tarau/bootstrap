package net.microfalx.bootstrap.core.utils;

import net.microfalx.lang.ClassUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all objects which need to be aware of their environment and collaborate with other
 * related beans with the need for specific injections.
 */
public abstract class ApplicationContextSupport implements ApplicationContextAware, BeanFactoryAware {

    private ApplicationContext applicationContext;
    private ListableBeanFactory beanFactory;

    /**
     * Return the bean instance that uniquely matches the given object type, if any.
     *
     * @param requiredType type the bean must match; can be an interface or superclass
     * @return an instance of the single bean matching the required type
     * @see ApplicationContext#getBean(Class)
     */
    public <T> T getBean(Class<T> requiredType) {
        return getApplicationContext().getBean(requiredType);
    }

    /**
     * Find all beans of a supplied annotation type.
     *
     * @param type the bean type
     * @param <T>  the bean type
     * @return a non-null instance
     */
    public <T> Collection<T> getBeansOfType(Class<T> type) {
        Map<String, T> beans = getBeanFactory().getBeansOfType(type);
        return unmodifiableCollection(beans.values());
    }

    /**
     * Find all beans which are annotated with the supplied annotation type.
     *
     * @param type the annotation type
     * @param <T>  the bean type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getBeansWithAnnotation(Class<? extends Annotation> type) {
        Map<String, T> beans = (Map<String, T>) getBeanFactory().getBeansWithAnnotation(type);
        return unmodifiableCollection(beans.values());
    }

    /**
     * Returns the application context.
     *
     * @return the context
     */
    public final ApplicationContext getApplicationContext() {
        if (applicationContext == null) throwIllegalState();
        return applicationContext;
    }

    /**
     * Returns the bean factory.
     *
     * @return a non-null instance
     */
    public final ListableBeanFactory getBeanFactory() {
        if (beanFactory == null) throwIllegalState();
        return beanFactory;
    }

    /**
     * Updates this class from another context class.
     *
     * @param contextSupport another context support class
     */
    public final void update(ApplicationContextSupport contextSupport) {
        requireNonNull(contextSupport);
        setApplicationContext(contextSupport.getApplicationContext());
        setBeanFactory(contextSupport.getBeanFactory());
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        requireNonNull(applicationContext);
        this.applicationContext = applicationContext;
    }

    @Override
    public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        requireNonNull(beanFactory);
        this.beanFactory = (ListableBeanFactory) beanFactory;
    }

    private void throwIllegalState() {
        throw new IllegalStateException(ClassUtils.getName(this) + " does not run in an ApplicationContext");
    }
}
