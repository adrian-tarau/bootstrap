package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.resource.ResourceLocation;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.resource.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourceServiceAnswerTest extends ServiceUnitTestCase {

    @Mock
    private ResourceService resourceService;

    @Test
    void sharedResource() {
        Resource resource = resourceService.getShared("sh1");
        assertNotNull(resource);
        resource = resourceService.get(ResourceLocation.SHARED, "sh1");
        assertNotNull(resource);
    }

    @Test
    void transientResource() {
        Resource resource = resourceService.getTransient("sh1");
        assertNotNull(resource);
        resource = resourceService.get(ResourceLocation.TRANSIENT, "sh1");
        assertNotNull(resource);
    }

    @Test
    void persistedResource() {
        Resource resource = resourceService.getPersisted("sh1");
        assertNotNull(resource);
        resource = resourceService.get(ResourceLocation.PERSISTED, "sh1");
        assertNotNull(resource);
    }

}