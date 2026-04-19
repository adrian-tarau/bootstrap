package net.microfalx.bootstrap.content;

import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.bootstrap.test.annotation.Subject;
import net.microfalx.resource.ClassPathResource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentExtractorTest extends ServiceUnitTestCase {

    @Subject
    private ContentService contentService;

    @Test
    void extractHtml() throws IOException {
        Content content = contentService.extract(ClassPathResource.file("test1.html"), true);
        Assertions.assertThat(content.getResource().loadAsString())
                .contains("Company A").contains("Contact us").contains("sidebar");
    }

    @Test
    void extractJson() throws IOException {
        Content content = contentService.extract(ClassPathResource.file("test1.json"), true);
        Assertions.assertThat(content.getResource().loadAsString())
                .contains("Click Here").contains("sun1.opacity").contains("Copy Again");
    }

    @Test
    void extractJson3() throws IOException {
        Content content = contentService.extract(ClassPathResource.file("test3.json"), true);
        Assertions.assertThat(content.getResource().loadAsString())
                .contains("John").contains("Report").contains("100.00%");
        assertEquals("Ping", content.getAttributes().get("dataTestName").getValue());
        assertEquals("0.00%", content.getAttributes().get("dataFailedPct").getValue());
    }

}