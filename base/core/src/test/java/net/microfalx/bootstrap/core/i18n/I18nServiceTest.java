package net.microfalx.bootstrap.core.i18n;

import net.microfalx.bootstrap.core.async.AsynchronousConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {MessageSource.class, I18nService.class})
@Import({I18nProperties.class, AsynchronousConfig.class})
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@SpringBootTest
class I18nServiceTest {

    @Autowired
    private I18nService i18nService;

    @BeforeEach
    void setup() throws Exception {
        i18nService.afterPropertiesSet();
    }

    @Test
    void getForEnum() {
        assertEquals("Success I18n", i18nService.getText(Status.SUCCESS));
        assertEquals("Failure I18n", i18nService.getText(Status.FAILURE));
        assertEquals("Other", i18nService.getText(Status.OTHER));
    }

    @Test
    void getForKey() {
        assertEquals("Success I18n", i18nService.getText("text.net.microfalx.bootstrap.core.i18n.status.success"));
        assertEquals("Failure I18n", i18nService.getText("text.net.microfalx.bootstrap.core.i18n.status.failure"));
        assertEquals("I18N(text.net.microfalx.bootstrap.core.i18n.status.other)", i18nService.getText("text.net.microfalx.bootstrap.core.i18n.status.other"));
    }

}