package net.microfalx.bootstrap.search;

import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IndexServiceTest extends AbstractSearchEngineTestCase {

    private Faker faker;

    @Autowired
    private IndexService indexService;

    @BeforeEach
    void before() {
        faker = new Faker();
        indexService.clear();
    }

    @AfterEach
    void after() {
        indexService.destroy();
    }

    @Test
    void initialize() {
        assertNotNull(indexService);
    }

    @Test
    void indexSingle() {
        doIndex(1);
        assertEquals(1, indexService.getDocumentCount());
        assertEquals(1, indexService.getPendingDocumentCount());
    }

    @Test
    void indexMultiple() {
        doIndex(10);
        assertEquals(10, indexService.getDocumentCount());
        assertEquals(10, indexService.getPendingDocumentCount());
    }

    @Test
    void Delete() {
        doIndex(10);
        indexService.remove("test1");
        assertEquals(9, indexService.getDocumentCount());
    }

    private void doIndex(int count) {
        Collection<Document> documents = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Document document = new Document("test" + i, "Test " + i);
            document.addTag("tag1");
            document.addAttribute("location", "Here");
            document.setDescription(faker.shakespeare().asYouLikeItQuote());
            documents.add(document);
        }
        indexService.index(documents);
    }

}