package net.microfalx.bootstrap.test.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A JUnit extension which replaces @{@link org.mockito.junit.jupiter.MockitoExtension}.
 * <p>
 * The extension does everything the mockito extension does plus adds supports for smarter mocking using
 * {@link net.microfalx.bootstrap.test.annotation.AnswerFor} and Instancio library ({@link org.instancio.junit.InstancioExtension}.
 */
public class BootstrapTestExtension implements BeforeEachCallback, AfterEachCallback {

    private TestSession testSession;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        testSession = new TestSession(testInstance);
        testSession.setup();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        testSession.shutdown();
    }


}
