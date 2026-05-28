package net.microfalx.bootstrap.resource;

import net.microfalx.resource.Resource;
import net.microfalx.threadpool.ThreadPool;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    private ResourceProperties properties = new ResourceProperties();

    @Mock
    private ThreadPool threadPool;

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

    @Test
    void resolve() throws IOException {
        Resource resource = resourceService.resolve(URI.create("classpath:/logger.xml"));
        assertNotNull(resource);
        Assertions.assertThat(resource.exists()).isTrue();

        resource = resourceService.resolve(URI.create("file:/tmp/bootstrap_test"));
        assertNotNull(resource);
        Assertions.assertThat(resource.exists()).isFalse();
    }


}