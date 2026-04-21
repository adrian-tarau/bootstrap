package net.microfalx.bootstrap.configuration;

import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.bootstrap.registry.RegistryService;
import net.microfalx.threadpool.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

    @Mock private Registry registry;
    @Mock private RegistryService registryService;
    @Mock private ThreadPool threadPool;

    @Spy private MockEnvironment environment = new MockEnvironment();

    @InjectMocks
    private ConfigurationService configurationService;

    @BeforeEach
    void setup() throws Exception {
        configurationService.afterPropertiesSet();
    }

    @Test
    void getMetadata() {
        assertEquals(2, configurationService.getRootMetadata().getChildren().size());
    }

    @Test
    void getRegistry() {
        when(registryService.getRegistry()).thenReturn(registry);
        assertNotNull(configurationService.getRegistry());
    }

    @Test
    void getProperty() {
        assertEquals("", configurationService.getProperty("a"));
        environment.setProperty("a", 1);
        assertEquals("1", configurationService.getProperty("a"));
    }

    @Test
    void getPropertyNormalized() {
        environment.setProperty("camelCase", 1);
        assertEquals("1", configurationService.getProperty("camelCase"));
        assertEquals("1", configurationService.getProperty("camel-case"));
        assertEquals("1", configurationService.getProperty("camel_case"));
    }

    @Test
    void getPropertyAsEnvironment() {
        environment.setProperty("A", 1);
        assertEquals("1", configurationService.getProperty("a"));
    }

}