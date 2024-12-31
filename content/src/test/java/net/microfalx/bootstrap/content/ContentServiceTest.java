package net.microfalx.bootstrap.content;

import net.microfalx.lang.StringUtils;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @InjectMocks
    private ContentService contentService;
    private final AtomicInteger elementCount = new AtomicInteger();
    private final AtomicInteger textCount = new AtomicInteger();

    @BeforeEach
    void setup() throws Exception {
        contentService.afterPropertiesSet();
    }

    @Test
    void detectMimeType() throws IOException {
        assertEquals("text/html", contentService.detectMimeType(ClassPathResource.file("test1.html")).toString());
        assertEquals("application/json", contentService.detectMimeType(ClassPathResource.file("test1.json")).toString());
        assertEquals("application/octet-stream", contentService.detectMimeType(ClassPathResource.file("test1.bin")).toString());
    }

    @Test
    void detectMimeTypeWithResourceResolver() throws IOException {
        assertEquals("text/html", ClassPathResource.file("test1.html").detectMimeType());
        assertEquals("application/json", ClassPathResource.file("test1.json").detectMimeType());
        assertEquals("application/octet-stream", ClassPathResource.file("test1.bin").detectMimeType());
    }

    @Test
    void parseHtml() throws IOException {
        contentService.parse(ClassPathResource.file("test1.html"), new CountElementsContentHandler());
        assertEquals(30, elementCount.get());
        assertEquals(40, textCount.get());
    }

    @Test
    void parseJson() throws IOException {
        contentService.parse(ClassPathResource.file("test1.json"), new CountElementsContentHandler());
        assertEquals(55, elementCount.get());
        assertEquals(130, textCount.get());
    }

    @Test
    void extractHtml() throws IOException {
        assertThat(contentService.extract(ClassPathResource.file("test1.html")).loadAsString())
                .contains("Company A").contains("Contact us").contains("sidebar");
    }

    @Test
    void extractJson() throws IOException {
        assertThat(contentService.extract(ClassPathResource.file("test1.json")).loadAsString())
                .contains("Click Here").contains("sun1.opacity").contains("Copy Again");
    }

    @Test
    void extractEmpty() throws IOException {
        assertThat(contentService.extract(Resource.text(null)).getResource().length())
                .isEqualTo(0);
    }

    @Test
    void resolveHtml() throws IOException {
        Content content = contentService.resolve(ContentLocator.create(ClassPathResource.file("test1.html")));
        assertNotNull(content);
        assertEquals("Test1", content.getName());
        assertEquals("text/html", content.getMimeType());
        assertThat(content.getUri().toASCIIString()).endsWith("test1.html");
    }

    @Test
    void resolveJson() throws IOException {
        Content content = contentService.resolve(ContentLocator.create(ClassPathResource.file("test1.json")));
        assertNotNull(content);
        assertEquals("Test1", content.getName());
        assertEquals("application/json", content.getMimeType());
        assertThat(content.getUri().toASCIIString()).endsWith("test1.json");
    }

    @Test
    void register() throws IOException {
        Content content = Content.create(ClassPathResource.file("test1.json"));
        String id = contentService.registerContent(content);
        Content locatedContent = contentService.getContent(id);
        assertEquals(content, locatedContent);
    }

    @Test
    void view() throws IOException {
        Resource resource = contentService.view(Content.create(ClassPathResource.file("1.txt")));
        assertTrue(resource.exists());
        assertEquals("view:test", resource.loadAsString());

        resource = contentService.view(Content.create(ClassPathResource.file("2.txt")));
        assertFalse(resource.exists());
        assertEquals("", resource.loadAsString());

        resource = contentService.view(Content.create(ClassPathResource.file("test2.json")));
        assertEquals(PRETTY_JSON2, resource.loadAsString());
    }

    @Test
    void edit() throws IOException {
        Resource resource = contentService.edit(Content.create(ClassPathResource.file("1.txt")));
        assertTrue(resource.exists());
        assertEquals("edit:test", resource.loadAsString());

        resource = contentService.edit(Content.create(ClassPathResource.file("2.txt")));
        assertFalse(resource.exists());
        assertEquals("", resource.loadAsString());

        resource = contentService.edit(Content.create(ClassPathResource.file("test2.json")));
        assertEquals(PRETTY_JSON2, resource.loadAsString());
    }

    @Test
    void update() throws IOException {
        contentService.update(Content.create(ClassPathResource.file("1.txt")));
        Assertions.assertThatThrownBy(() -> contentService.update(Content.create(ClassPathResource.file("2.txt")))).isExactlyInstanceOf(ContentException.class);
    }

    class CountElementsContentHandler implements ContentHandler {

        @Override
        public void setDocumentLocator(Locator locator) {

        }

        @Override
        public void startDocument() throws SAXException {

        }

        @Override
        public void endDocument() throws SAXException {

        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {

        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {

        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            elementCount.incrementAndGet();
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String string = new String(ch);
            if (StringUtils.isNotEmpty(string)) textCount.incrementAndGet();
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {

        }

        @Override
        public void skippedEntity(String name) throws SAXException {

        }
    }

    private static final String PRETTY_JSON2= "{\r\n" +
            "  \"widget\" : {\r\n" +
            "    \"debug\" : \"on\",\r\n" +
            "    \"window\" : {\r\n" +
            "      \"name\" : \"main_window\",\r\n" +
            "      \"width\" : 500,\r\n" +
            "      \"height\" : 500\r\n" +
            "    }\r\n" +
            "  }\r\n" +
            "}";

}