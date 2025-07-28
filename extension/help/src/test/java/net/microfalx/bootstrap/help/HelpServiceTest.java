package net.microfalx.bootstrap.help;

import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.threadpool.ThreadPool;
import org.assertj.core.api.Assertions;
import org.joor.Reflect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HelpServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpServiceTest.class);

    @Mock
    private IndexService indexService;

    @InjectMocks
    private HelpService helpService;

    @BeforeEach
    void setup() throws Exception {
        Reflect.on(helpService).set("threadPool", ThreadPool.get());
        helpService.afterPropertiesSet();
    }

    @Test
    void renderSimple() {
        render("simple");
    }

    @Test
    void renderTables() {
        render("table");
    }

    @Test
    void renderImages() {
        render("image");
    }

    @Test
    void renderAll() throws IOException {
        Assertions.assertThat(helpService.renderAll().loadAsString())
                .contains("Paragraphs are separated").contains("Quick, count to ten!")
                .contains("table table-striped");
    }

    @Test
    void tocs() {
        assertEquals(4, helpService.getRoot().getChildren().size());
    }

    private void render(String path) {
        StringWriter writer = new StringWriter();
        try {
            helpService.render(path, writer);
            LOGGER.info("Rendered HTML for '{}'\n\n\n{}", path, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to render help '" + path + "'", e);
        }
    }

}