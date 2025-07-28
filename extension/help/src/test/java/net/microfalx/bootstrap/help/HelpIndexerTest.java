package net.microfalx.bootstrap.help;

import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.threadpool.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HelpIndexerTest {

    @Mock
    private IndexService indexService;

    @Mock
    private ThreadPool threadPool;

    @InjectMocks
    private HelpService helpService;

    @BeforeEach
    void setup() throws Exception {
        helpService.afterPropertiesSet();
    }

    @Test
    void indexTocs() {
        HelpIndexer indexer = new HelpIndexer(helpService, indexService, helpService.getRoot());
        indexer.run();
        verify(indexService, times(6)).index(any(Document.class));
    }

}