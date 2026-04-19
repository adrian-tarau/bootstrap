package net.microfalx.bootstrap.test.extension;

import net.microfalx.bootstrap.test.TestContext;
import org.junit.jupiter.api.extension.*;

/**
 * A JUnit extension which replaces @{@link org.mockito.junit.jupiter.MockitoExtension}.
 * <p>
 * The extension does everything the mockito extension does plus adds supports for smarter mocking using
 * {@link net.microfalx.bootstrap.test.annotation.AnswerFor} and Instancio library ({@link org.instancio.junit.InstancioExtension}.
 */
public class BootstrapExtension implements BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, AfterEachCallback {


    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        TestContext testTestContext = new TestContext(context.getRequiredTestClass());
        testTestContext.beforeAll();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        TestContext.current().ifPresent(TestContext::afterAll);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        TestContext.current().ifPresent(session -> session.beforeEach(testInstance));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        TestContext.current().ifPresent(TestContext::afterEach);
    }


}
