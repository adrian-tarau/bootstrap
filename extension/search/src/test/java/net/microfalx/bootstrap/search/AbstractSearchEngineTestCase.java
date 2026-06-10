package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.core.async.AsynchronousConfiguration;
import net.microfalx.bootstrap.core.i18n.I18nProperties;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {ResourceService.class, ResourceProperties.class,
        I18nService.class, I18nProperties.class,
        IndexService.class, IndexProperties.class, SearchProperties.class, SearchService.class, ContentService.class})
@Import({AsynchronousConfiguration.class})
@SpringBootTest
public abstract class AbstractSearchEngineTestCase {
}
