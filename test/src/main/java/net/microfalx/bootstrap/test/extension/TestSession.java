package net.microfalx.bootstrap.test.extension;

import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds instances for a test session
 */
public class TestSession {

    private final Object testInstance;

    private AnnotationConfigApplicationContext applicationContext;
    private final Map<Class<?>, Answer<?>> answers = new HashMap<>();
    private final Map<Class<?>, Object> objects = new HashMap<>();

    TestSession(Object testInstance) {
        requireNonNull(testInstance);
        this.testInstance = testInstance;
        this.applicationContext = new AnnotationConfigApplicationContext();
    }

    public void setup() {

    }

    public void shutdown() {

    }

    public <T> T lookup(Class<T> type) {
        return null;
    }

    private void createService(Class<?> serviceClass) {

    }

    private void createObject(Class<?> serviceClass) {

    }

    private static final Collection<Class<?>> realProperties = List.of(
            ResourceProperties.class
    );

    private static final Collection<Class<?>> realComponent = List.of(
            OptionalValidatorFactoryBean.class
    );

    private static final Collection<Class<?>> realServices = List.of(
            I18nService.class, ResourceService.class, MetadataService.class
    );

}
