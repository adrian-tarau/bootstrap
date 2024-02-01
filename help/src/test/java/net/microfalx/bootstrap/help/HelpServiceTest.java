package net.microfalx.bootstrap.help;

import net.microfalx.bootstrap.search.IndexService;
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

@ExtendWith(MockitoExtension.class)
class HelpServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpServiceTest.class);

    @Mock
    private IndexService indexService;

    @InjectMocks
    private HelpService helpService;

    @BeforeEach
    void setup() throws Exception {
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