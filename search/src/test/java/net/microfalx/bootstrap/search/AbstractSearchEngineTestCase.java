package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.core.async.AsyncConfig;
import net.microfalx.bootstrap.core.i18n.I18nConfig;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {ResourceService.class, ResourceProperties.class,
        IndexService.class, IndexProperties.class, SearchProperties.class, SearchService.class})
@Import({I18nConfig.class, AsyncConfig.class})
@SpringBootTest
public abstract class AbstractSearchEngineTestCase {
}
