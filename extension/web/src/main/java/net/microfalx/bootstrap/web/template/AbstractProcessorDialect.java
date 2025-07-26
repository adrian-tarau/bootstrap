package net.microfalx.bootstrap.web.template;

import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.ClassUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Base class for our dialects, which is application context aware.
 */
public abstract class AbstractProcessorDialect extends org.thymeleaf.dialect.AbstractProcessorDialect {

    protected final ApplicationContext applicationContext;

    public AbstractProcessorDialect(String name, String prefix, int processorPrecedence, ApplicationContext applicationContext) {
        super(name, prefix, processorPrecedence);
        ArgumentUtils.requireNonNull(applicationContext);
        this.applicationContext = applicationContext;
    }

    /**
     * Creates an instance of a given class and injects the application context.
     *
     * @param clazz the class
     * @param <T>   the instance type
     * @return a new instance
     */
    protected final <T> T createInstance(Class<T> clazz) {
        T instance = ClassUtils.create(clazz);
        if (instance instanceof ApplicationContextAware aca) aca.setApplicationContext(applicationContext);
        return instance;
    }
}
