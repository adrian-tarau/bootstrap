package net.microfalx.bootstrap.test.extension;

import net.microfalx.bootstrap.core.i18n.I18nService;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds instances for a test session
 */
public class Session {

    private final Object testInstance;

    private AnnotationConfigApplicationContext applicationContext;
    private final Map<Class<?>, Answer<?>> answers = new HashMap<>();
    private final Map<Class<?>, Object> objects = new HashMap<>();

    Session(Object testInstance) {
        requireNonNull(testInstance);
        this.testInstance = testInstance;
        this.applicationContext = new AnnotationConfigApplicationContext();
    }

    public void setup() {
        initCoreServices();
    }

    public void shutdown() {

    }

    public <T> T lookup(Class<T> type) {
        return null;
    }

    private void initCoreServices() {
        serviceClasses.forEach(this::createService);
        Settings settings = Settings.create()
                .set(Keys.COLLECTION_MIN_SIZE, 50)
                .set(Keys.COLLECTION_MAX_SIZE, 100);
    }

    private void createService(Class<?> serviceClass) {

    }

    private void createObject(Class<?> serviceClass) {

    }

    private static final Collection<Class<?>> serviceClasses = List.of(I18nService.class);

}
