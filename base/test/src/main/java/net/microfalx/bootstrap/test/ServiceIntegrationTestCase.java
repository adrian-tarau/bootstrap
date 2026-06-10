package net.microfalx.bootstrap.test;

import net.microfalx.bootstrap.test.annotation.ServiceIntegrationTest;

/**
 * Base class for all Spring Boot integration test classes.
 * <p>
 * Alternatively, {@link ServiceIntegrationTest} can be used but inheritance would allow for an
 * easier navigation in the IDE.
 */
@ServiceIntegrationTest
public abstract class ServiceIntegrationTestCase {
}
