package net.microfalx.bootstrap.test.extension;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.test.annotation.AnswerFor;
import net.microfalx.bootstrap.test.annotation.Prepare;
import net.microfalx.bootstrap.test.annotation.Subject;
import net.microfalx.bootstrap.test.answer.AbstractAnswer;
import net.microfalx.lang.*;
import net.microfalx.threadpool.ThreadPool;
import org.atteo.classindex.ClassIndex;
import org.mockito.Mock;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;

/**
 * Holds instances for a test session
 */
@Slf4j
public class Session {

    private final Class<?> testClass;
    private final List<Field> testClassFields = new ArrayList<>();
    private Object testInstance;

    private AnnotationConfigApplicationContext applicationContext;
    private final Map<Class<?>, Class<?>> answersClasses = new HashMap<>();
    private final Set<Class<?>> subjectClasses = new LinkedHashSet<>();
    private Map<Class<?>, Object> objects = new HashMap<>();
    private final Set<Class<?>> mockClasses = new LinkedHashSet<>();
    private final List<ComponentCreator<?>> componentCreators = new ArrayList<>();

    private File workingDirectory;

    static ThreadLocal<Session> CURRENT = new ThreadLocal<>();

    /**
     * Returns the session attached to the current thread.
     *
     * @return a non-null instance
     */
    public static Optional<Session> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public Session(Class<?> testClass) {
        requireNonNull(testClass);
        this.testClass = testClass;
    }

    /**
     * Invoked during the setup of the test class, to initialize metadata.
     */
    public void beforeAll() {
        CURRENT.set(this);
        initSettings();
        initFields();
        initComponentCreators();
        discoverAnswersClasses();
        scanPrepareAnnotations();
        scanTestClasses();
    }

    /**
     * Invoked during the teardown of the test class, to cleanup.
     */
    public void afterAll() {
        CURRENT.remove();
    }

    /**
     * Invoked before a test method is called, to setup the test class.
     *
     * @param testInstance the instance of the test class
     */
    public void beforeEach(Object testInstance) {
        this.testInstance = testInstance;
        initDirectory();
        initContext();
        initCoreServices();
        injectTestInstances();
    }

    /**
     * Invoked after a test method is called, to destroy and cleanup the test class.
     */
    public void afterEach() {
        // nothing to cleanup for now
    }

    /**
     * Returns the working directory associated with the tests.
     * <p>
     * Each test (method) receives its own working directory
     *
     * @return a non-null instance
     */
    public File getWorkingDirectory() {
        if (workingDirectory == null) throw new IllegalStateException("Working directory not set");
        return workingDirectory;
    }

    /**
     * Returns the Spring application context for the current test.
     *
     * @return a non-null instance
     */
    public ApplicationContext getApplicationContext() {
        if (applicationContext == null) throw new IllegalStateException("Application context not set");
        return applicationContext;
    }

    /**
     * Returns the subjects (real objects, directly and indirect under test).
     *
     * @return a non-null instance
     */
    public Set<Class<?>> getSubjectClasses() {
        return unmodifiableSet(subjectClasses);
    }

    /**
     * Returns the mocks (directly and indirect under test).
     *
     * @return a non-null instance
     */
    public Set<Class<?>> getMockClasses() {
        return unmodifiableSet(mockClasses);
    }

    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<T> type) {
        requireNonNull(type);
        T instance = (T) objects.get(type);
        if (instance == null) {
            throw new IllegalArgumentException("An object with type '" + ClassUtils.getName(type) + "' is not available");
        }
        return instance;
    }

    private void initCoreServices() {
        applicationContext = new AnnotationConfigApplicationContext();
        registerCoreMocksWithContext();
        registerPreparedWithContext();
        applicationContext.register(getSubjectClasses().toArray(new Class[0]));
        applicationContext.refresh();
        updateObjectsFromApplicationContext();
    }

    private <T> void registerCoreMocksWithContext() {
        registerMockWithContext(ScheduledExecutorService.class);
    }

    private <T> void registerPreparedWithContext() {
        for (Class<?> mockClass : mockClasses) {
            registerMockWithContext(mockClass);
        }
    }

    private <T> void registerMockWithContext(Class<T> type) {
        applicationContext.registerBean(type, () -> mock(type));
    }

    private void createComponent(Class<?> componentClass) {
        applicationContext.register(componentClass);
        applicationContext.refresh();
        registerObject(componentClass, applicationContext.getBean(componentClass));
    }

    private void initializeComponent(Object component) {
        if (component instanceof InitializingBean initializingBean) {
            try {
                initializingBean.afterPropertiesSet();
            } catch (Exception e) {
                ExceptionUtils.rethrowException(e);
            }
        }
    }

    private void registerObject(Class<?> type, Object instance) {
        initializeComponent(instance);
        objects.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    private <T> T mock(Class<T> type) {
        T instance = (T) objects.get(type);
        if (instance != null) return instance;
        Class<?> answerClass = answersClasses.get(type);
        if (answerClass != null) {
            try {
                Answer<?> answer = (Answer<?>) ClassUtils.create(answerClass);
                if (answer instanceof AbstractAnswer abstractAnswer) {
                    abstractAnswer.initialize(this);
                }
                instance = Mockito.mock(type, answer);
            } catch (Exception e) {
                return rethrowExceptionAndReturn(e);
            }
        } else {
            instance = Mockito.mock(type, Mockito.RETURNS_SMART_NULLS);
        }
        registerObject(type, instance);
        return instance;
    }

    private void createObject(Class<?> serviceClass) {

    }

    private void initContext() {
        this.applicationContext = new AnnotationConfigApplicationContext();
        this.objects = new HashMap<>();
        this.objects.put(Session.class, this);
    }

    private void initSettings() {

    }

    private void initDirectory() {
        workingDirectory = new File(JvmUtils.getWorkingDirectory(), "target");
        if (!workingDirectory.exists()) workingDirectory = JvmUtils.getTemporaryDirectory();
        workingDirectory = validateDirectoryExists(new File(workingDirectory, IdGenerator.get().nextAsString()));
        LOGGER.info("Working director for '{}' is '{}'", ClassUtils.getName(testClass), workingDirectory);
        JvmUtils.setCacheDirectory(workingDirectory);
    }

    private void initComponentCreators() {
        ClassIndex.getSubclasses(ComponentCreator.class).forEach(cc -> {
            componentCreators.add(ClassUtils.create(cc));
        });
        AnnotationUtils.sort(componentCreators);
        LOGGER.info("Loaded {} component creators", componentCreators.size());
    }

    private void discoverAnswersClasses() {
        for (Class<?> answerClass : ClassIndex.getAnnotated(AnswerFor.class)) {
            AnswerFor answerForAnnot = answerClass.getAnnotation(AnswerFor.class);
            Class<?>[] componentTypes = answerForAnnot.value();
            for (Class<?> componentType : componentTypes) {
                answersClasses.put(componentType, answerClass);
            }
        }
        LOGGER.info("Loaded {} answers", answersClasses.size());
    }

    private void scanPrepareAnnotations() {
        mockClasses.addAll(Arrays.asList(Executor.class, ExecutorService.class, ScheduledExecutorService.class,
                ThreadPool.class));
        Collection<Prepare> prepareAnnotations = AnnotationUtils.getAnnotations(testClass, Prepare.class);
        for (Prepare prepareAnnotation : prepareAnnotations) {
            mockClasses.addAll(Arrays.asList(prepareAnnotation.mocks()));
            subjectClasses.addAll(Arrays.asList(prepareAnnotation.subjects()));
        }
    }

    private void scanTestClasses() {
        for (Field field : testClassFields) {
            if (field.isAnnotationPresent(Mock.class)) {
                mockClasses.add(field.getType());
            }
            if (field.isAnnotationPresent(Subject.class)) {
                subjectClasses.add(field.getType());
            }
        }
    }

    private void initFields() {
        this.testClassFields.addAll(ReflectionUtils.openFields(testClass));
    }

    private void updateObjectsFromApplicationContext() {
        for (Class<?> type : subjectClasses) {
            Object bean = applicationContext.getBean(type);
            objects.put(type, bean);
        }
    }

    private void injectTestInstances() {
        for (Field field : testClassFields) {
            try {
                Object currentFieldValue = field.get(testInstance);
                Class<?> fieldType = field.getType();
                Object preparedFieldValue = objects.get(fieldType);
                boolean shouldSet = currentFieldValue == null || preparedFieldValue != null;
                if (currentFieldValue != null) {
                    // if already a mock, and we have a new mock, replace
                    MockingDetails mockingDetails = Mockito.mockingDetails(currentFieldValue);
                    shouldSet = mockingDetails.isMock();
                }
                if (preparedFieldValue != null && shouldSet) {
                    field.set(testInstance, preparedFieldValue);
                }
            } catch (Exception e) {
                ExceptionUtils.rethrowException(e);
            }
        }
    }


}
