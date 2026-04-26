package net.microfalx.bootstrap.configuration;

import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.bootstrap.registry.RegistryService;
import net.microfalx.bootstrap.registry.Storage;
import net.microfalx.threadpool.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.mock.env.MockEnvironment;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractConfigurationTestCase {

    @Mock protected RegistryService registryService;
    @Mock private ThreadPool threadPool;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Spy protected ConversionService conversionService = DefaultConversionService.getSharedInstance();
    @Spy protected Registry registry = Registry.create(Storage.create());
    @Spy protected MockEnvironment environment = new MockEnvironment();

    @InjectMocks
    protected ConfigurationService configurationService;

    @BeforeEach
    void setup() throws Exception {
        configurationService.afterPropertiesSet();
        postSetup();
    }

    protected void postSetup() {

    }

    protected void mockRegistry() {
        when(registryService.getRegistry()).thenReturn(registry);
    }

}
