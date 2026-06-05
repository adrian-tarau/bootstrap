package net.microfalx.bootstrap.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Spy
    private ApplicationProperties properties = new ApplicationProperties();

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeEach
    void setup() throws Exception {
        applicationService.afterPropertiesSet();
    }

    @Test
    void getApplication() {
        Application application = applicationService.getApplication();
        assertNotNull(application);
        assertEquals("Default", application.getName());
        assertEquals("1.0.0", application.getVersion());
        assertEquals("N/A", application.getBuildNumber());
        assertEquals("N/A", application.getBuildTime());
        assertEquals("#", application.getUrl());
    }

}