package net.microfalx.bootstrap.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SearchServiceTest extends AbstractSearchEngineTestCase {

    @Autowired
    private IndexService indexService;

    @Autowired
    private SearchService searchService;

    private DataGenerator dataGenerator;

    @BeforeEach
    void before() {
        dataGenerator = new DataGenerator(indexService);
        dataGenerator.execute();
    }

    @Test
    void initialize() {
        assertNotNull(searchService);
    }

}