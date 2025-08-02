package net.microfalx.bootstrap.help;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TocRendererTest {

    private final Toc root = new Toc();
    private HelpLoader loader;

    @BeforeEach
    void setup() {
        loader = new HelpLoader();
        loader.load(root);
    }

    @Test
    void render() {
        TocRenderer renderer = new TocRenderer(root, RenderingOptions.DEFAULT);
        Assertions.assertThat(renderer.render()).contains("xxx");
    }

}