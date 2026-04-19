package net.microfalx.bootstrap.content;

import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.bootstrap.test.annotation.Subject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FragmentLoaderTest extends ServiceUnitTestCase {

    @Subject
    private ContentService contentService;

    @Subject
    private FragmentLoader loader;

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