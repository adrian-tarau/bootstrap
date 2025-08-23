package net.microfalx.bootstrap.help;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.core.async.AsynchronousConfig;
import net.microfalx.bootstrap.core.i18n.I18nProperties;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = {I18nService.class, ResourceService.class, ResourceProperties.class, OptionalValidatorFactoryBean.class,
        MetadataService.class, ContentService.class, IndexService.class, SearchService.class, HelpService.class})
@Import({I18nProperties.class, AsynchronousConfig.class})
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@SpringBootTest
public class HelpServiceIntegrationTest {

    @Autowired
    private HelpService helpService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private SearchService searchService;

    @BeforeEach
    void setup() {
        await().atMost(Duration.ofSeconds(30)).until(() -> helpService.isReady());
        indexService.flush();
        searchService.reload();
    }

    @Test
    void queryHelp() {
        List<Toc> tocs = helpService.search("typographer converted syntax", 0, 20);
        assertTrue(tocs.size() > 1);
    }
}
