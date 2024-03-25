package net.microfalx.bootstrap.content;

import net.microfalx.resource.ClassPathResource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class ContentExtractorTest {

    @InjectMocks
    private ContentService contentService;

    @BeforeEach
    void setup() throws Exception {
        contentService.afterPropertiesSet();
    }

    @Test
    void extractHtml() throws IOException {
        Content content = contentService.extract(ClassPathResource.file("test1.html"));
        Assertions.assertThat(content.getResource().loadAsString())
                .contains("Company A").contains("Contact us").contains("sidebar");
    }

    @Test
    void extractJson() throws IOException {
        Content content = contentService.extract(ClassPathResource.file("test1.json"));
        Assertions.assertThat(content.getResource().loadAsString())
                .contains("Click Here").contains("sun1.opacity").contains("Copy Again");
    }

}