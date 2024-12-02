package net.microfalx.bootstrap.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class FragmentLoaderTest {

    @InjectMocks
    private ContentService contentService;

    private FragmentLoader loader;

    @BeforeEach
    void setup() {
        loader = new FragmentLoader(contentService);
    }

    @Test
    void loadSnippet() throws IOException {
        loader.load();
        Fragment fragment = contentService.getFragment("snippet1");
        assertEquals("Snippet 1", fragment.getName());
        assertEquals(Fragment.Type.SNIPPET, fragment.getType());
        assertEquals(Fragment.Language.JAVASCRIPT, fragment.getLanguage());
        assertThat(fragment.getResource().loadAsString()).isEqualToIgnoringNewLines("function test() {\n" +
                "\n" +
                "}");
    }

    @Test
    void loadExample() throws IOException {
        loader.load();
        Fragment fragment = contentService.getFragment("example1");
        assertEquals("Example 1", fragment.getName());
        assertEquals(Fragment.Type.EXAMPLE, fragment.getType());
        assertEquals(Fragment.Language.JAVA, fragment.getLanguage());
        assertThat(fragment.getResource().loadAsString()).isEqualToIgnoringNewLines("package test;\n" +
                "\n" +
                "public class A {\n" +
                "\n" +
                "}");
    }

}