package net.microfalx.bootstrap.help;

import net.microfalx.lang.NamedIdentityAware;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.util.stream.Collectors.joining;
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
        assertEquals(0, root.getDepth());
    }

    @Test
    void loadSimple() throws IOException {
        Toc toc = root.findById("simple");
        assertNotNull(toc);
        assertEquals("Simple", toc.getName());
        assertEquals(1, toc.getDepth());
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
        assertEquals(1, toc.getDepth());
        assertEquals(4, toc.getChildren().size());

        toc = root.findByPath("complex/child1");
        assertNotNull(toc);
        assertEquals("Child 1", toc.getName());
        assertEquals(2, toc.getDepth());

        toc = root.findByPath("complex/not_found");
        assertNull(toc);
    }

    @Test
    void loadBreadcrumb() {
        Toc toc = root.findByPath("complex/child1");
        assertEquals("Complex > Child 1", toc.getParents().stream().map(NamedIdentityAware::getName)
                .collect(joining(" > ")));
    }

}