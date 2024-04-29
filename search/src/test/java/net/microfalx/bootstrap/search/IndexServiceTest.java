package net.microfalx.bootstrap.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IndexServiceTest extends AbstractSearchEngineTestCase {

    @Autowired
    private IndexService indexService;

    private DataGenerator dataGenerator;

    @BeforeEach
    void before() {
        indexService.clear();
        dataGenerator = new DataGenerator(indexService);
    }

    @Test
    void initialize() {
        assertNotNull(indexService);
    }

    @Test
    void indexSingle() {
        dataGenerator.setDocumentCount(1).execute();
        assertEquals(1, indexService.getDocumentCount());
    }

    @Test
    void indexMultiple() {
        dataGenerator.execute();
        assertEquals(dataGenerator.getDocumentCount(), indexService.getDocumentCount());
        assertTrue(indexService.getPendingDocumentCount() >= 0);
    }

    @Test
    void delete() {
        Set<String> ids = dataGenerator.setDocumentCount(10).execute();
        indexService.remove(ids.iterator().next());
        indexService.commit();
        assertEquals(9, indexService.getDocumentCount());
    }


}