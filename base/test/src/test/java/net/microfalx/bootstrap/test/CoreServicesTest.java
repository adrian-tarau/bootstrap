package net.microfalx.bootstrap.test;

import net.microfalx.bootstrap.configuration.ConfigurationService;
import net.microfalx.bootstrap.registry.RegistryService;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.store.StoreService;
import net.microfalx.bootstrap.test.annotation.Prepare;
import net.microfalx.bootstrap.test.annotation.Subject;
import net.microfalx.threadpool.ThreadPool;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Prepare(mocks = RegistryService.class, subjects = ConfigurationService.class)
public class CoreServicesTest extends ServiceUnitTestCase {

    @Mock
    private ThreadPool threadPool;

    @Mock
    private StoreService storeService;

    @Subject
    private ResourceService resourceService;

    @Subject
    private TestService testService;

    private TestContext testContext;

    @Test
    void checkSession() {
        assertNotNull(testContext);
    }

    @Test
    void checkMocks() {
        assertNotNull(threadPool);
        Set<Class<?>> mockClasses = testContext.getMockClasses();
        assertEquals(6, mockClasses.size());
        assertTrue(mockClasses.contains(RegistryService.class));
        assertTrue(mockClasses.contains(StoreService.class));
    }

    @Test
    void checkSubjects() {
        Set<Class<?>> subjectClasses = testContext.getSubjectClasses();
        assertEquals(3, subjectClasses.size());
        assertTrue(subjectClasses.contains(ResourceService.class));
        assertTrue(subjectClasses.contains(ConfigurationService.class));
        assertTrue(subjectClasses.contains(TestService.class));

        assertNotNull(resourceService);
        assertNotNull(resourceService.getShared("a"));
        assertNotNull(resourceService.getPersisted("a"));
        assertNotNull(resourceService.getTransient("a"));
    }
}
