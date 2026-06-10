package net.microfalx.bootstrap.test;

import net.microfalx.bootstrap.test.annotation.ServiceUnitTest;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Base class for all Spring Boot unit test classes.
 * <p>
 * Alternatively, {@link ServiceUnitTest} can be used but inheritance would allow for an
 * easier navigation in the IDE.
 */
@ServiceUnitTest
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class ServiceUnitTestCase {
}
