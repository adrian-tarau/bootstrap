package net.microfalx.bootstrap.help;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.test.AbstractBootstrapServiceTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = {ContentService.class, IndexService.class, SearchService.class,
        HelpService.class})
public class HelpServiceIntegrationTest extends AbstractBootstrapServiceTestCase {

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
