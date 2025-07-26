package net.microfalx.bootstrap.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    private ResourceProperties properties = new ResourceProperties();

    @InjectMocks
    private ResourceService resourceService;

    @BeforeEach
    void setup() throws Exception {
        resourceService.afterPropertiesSet();
    }

    @Test
    void getPersisted() {
        assertNotNull(resourceService.getPersisted("p1"));
    }

    @Test
    void getShared() {
        assertNotNull(resourceService.getShared("s1"));
    }

    @Test
    void getTransient() {
        assertNotNull(resourceService.getTransient("t1"));
    }


}