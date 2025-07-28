package net.microfalx.bootstrap.help;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HelpLoaderTest {

    private final Toc root = new Toc();
    private HelpLoader loader;

    @BeforeEach
    void setup() {
        loader = new HelpLoader();
        loader.load(root);
    }

    @Test
    void loadCount() throws IOException {
        assertEquals(8, loader.tocCount);
    }

    @Test
    void loadSimple() throws IOException {
        Toc toc = root.findById("simple");
        assertNotNull(toc);
        assertEquals("Simple", toc.getName());
        Assertions.assertThat(toc.getContent().loadAsString())
                .contains("Paragraphs are separated ").contains("* this one");

        toc = root.findById("not_found");
        assertNull(toc);
    }

    @Test
    void loadComplex() {
        Toc toc = root.findById("complex");
        assertNotNull(toc);
        assertEquals("Complex", toc.getName());
        assertEquals(4, toc.getChildren().size());

        toc = root.findByPath("complex/child1");
        assertNotNull(toc);
        assertEquals("Child 1", toc.getName());

        toc = root.findByPath("complex/not_found");
        assertNull(toc);
    }

}