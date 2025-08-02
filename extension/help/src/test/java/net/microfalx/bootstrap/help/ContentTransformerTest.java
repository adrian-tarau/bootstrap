package net.microfalx.bootstrap.help;

import net.microfalx.resource.ClassPathResource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTransformerTest {

    @Test
    void simpleNoIndentation() throws IOException {
        ContentTransformer transformer = new ContentTransformer(ClassPathResource.file("help/complex/child1.md"));
        assertThat(transformer.execute().loadAsString())
                .contains("## Section 1").contains("### Section 1-2");
    }

    @Test
    void simpleOneLevelIndentation() throws IOException {
        ContentTransformer transformer = new ContentTransformer(ClassPathResource.file("help/complex/child1.md"))
                .setOptions(RenderingOptions.builder().level(1).build());
        assertThat(transformer.execute().loadAsString())
                .contains("### Section 1").contains("#### Section 1-2");
    }

    @Test
    void simpleWithToc() throws IOException {
        Toc toc = new Toc("child1", "Child 1")
                .withContent(ClassPathResource.file("help/complex/child1.md"));
        ContentTransformer transformer = new ContentTransformer(toc.getContent())
                .setToc(toc).setOptions(RenderingOptions.builder().level(1).build());
        assertThat(transformer.execute().loadAsString())
                .doesNotContain("## Child 1")
                .contains("### Section 1").contains("#### Section 1-2");

        transformer.setOptions(RenderingOptions.builder().heading(true).level(1).build());
        assertThat(transformer.execute().loadAsString())
                .contains("## Child 1")
                .contains("### Section 1").contains("#### Section 1-2");
    }

    @Test
    void simpleWithNavigation() throws IOException {
        Toc toc = new Toc("child1", "Child 1")
                .withContent(ClassPathResource.file("help/complex/child1.md"));
        ContentTransformer transformer = new ContentTransformer(toc.getContent())
                .setToc(toc).setOptions(RenderingOptions.builder().heading(true)
                        .navigation(true).level(1).build());
        assertThat(transformer.execute().loadAsString())
                .contains("## Child 1")
                .contains("### Navigation").contains("Go to")
                .contains("### Section 1").contains("#### Section 1-2");
    }

}