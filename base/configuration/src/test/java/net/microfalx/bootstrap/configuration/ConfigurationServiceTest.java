package net.microfalx.bootstrap.configuration;

import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.bootstrap.registry.RegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

    @Mock
    private Registry registry;

    @Mock
    private RegistryService registryService;

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

}