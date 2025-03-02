package net.microfalx.bootstrap.search;

import net.microfalx.metrics.Matrix;
import net.microfalx.metrics.Query;
import net.microfalx.metrics.Result;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

class SearchRepositoryTest extends AbstractSearchEngineTestCase {

    @Autowired
    private IndexService indexService;

    @Autowired
    private ApplicationContext applicationContext;

    private SearchRepository searchRepository;

    private DataGenerator dataGenerator;

    @BeforeEach
    void setup() {
        dataGenerator = new DataGenerator(indexService);
        searchRepository = new SearchRepository();
        searchRepository.setApplicationContext(applicationContext);
        dataGenerator.execute();
    }

    @Test
    void documentsTrend() {
        Result result = searchRepository.query(Query.create(SearchRepository.DOCUMENT_QUERY_TYPE));
        Matrix matrix = result.getMatrixes().iterator().next();
        Assertions.assertThat(matrix.getValues().size()).isGreaterThan(40);
    }

    @Test
    void allFieldsTrend() {
        Result result = searchRepository.query(Query.create(SearchRepository.FIELD_QUERY_TYPE));
        Assertions.assertThat(result.getMatrixes().size()).isGreaterThan(20);
        Matrix matrix = result.getMatrixes().iterator().next();
        Assertions.assertThat(matrix.getValues().size()).isGreaterThan(0);
    }

    @Test
    void ownerTrend() {
        String firstOwner = dataGenerator.getAttribute(Document.OWNER_FIELD).iterator().next();
        Result result = searchRepository.query(Query.create(SearchRepository.FIELD_QUERY_TYPE, Document.OWNER_FIELD + ":" + firstOwner));
        Assertions.assertThat(result.getMatrixes().size()).isGreaterThan(0);
        Matrix matrix = result.getMatrixes().iterator().next();
        Assertions.assertThat(matrix.getValues().size()).isGreaterThan(0);
    }

}