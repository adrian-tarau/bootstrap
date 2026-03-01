package net.microfalx.bootstrap.ai.lucene;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import lombok.AccessLevel;
import lombok.Getter;
import net.microfalx.bootstrap.ai.api.AiService;
import net.microfalx.bootstrap.search.*;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.metrics.Metrics;
import net.microfalx.threadpool.ThreadPool;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class LuceneEmbeddingModel implements IndexListener, EmbeddingModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneEmbeddingModel.class);

    static Metrics INDEX_METRICS = Metrics.of("Lucene Embedding");
    static Metrics SEARCH_METRICS = Metrics.of("Lucene Embedding");

    @Getter(AccessLevel.PROTECTED)
    private final AiService aiService;
    @Getter(AccessLevel.PROTECTED)
    private final IndexService indexService;
    @Getter(AccessLevel.PROTECTED)
    private final SearchService searchService;
    private Encoding encoding;

    private Indexer indexer;
    private ThreadPool threadPool;
    private boolean enabled = false;

    private volatile LuceneContentRetriever contentRetriever;

    public LuceneEmbeddingModel(AiService aiService, IndexService indexService, SearchService searchService) {
        requireNonNull(indexService);
        requireNonNull(indexService);
        requireNonNull(searchService);
        this.aiService = aiService;
        this.indexService = indexService;
        this.searchService = searchService;
        initIndex();
        initEncodings();
    }

    public ThreadPool getThreadPool() {
        return threadPool != null ? threadPool : indexService.getThreadPool();
    }

    public Indexer getIndexer() {
        return indexer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LuceneEmbeddingModel setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    public LuceneEmbeddingModel setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public void afterIndexing(Collection<Document> documents) {
        requireNonNull(documents);
        if (!enabled) return;
        for (Document document : documents) {
            getThreadPool().execute(() -> index(document));
        }
    }

    public void index(Document document) {
        AtomicReference<Embedding> embedding = new AtomicReference<>();
        INDEX_METRICS.time("Extract", (t) -> {
            TextExtractor textExtractor = new TextExtractor(getIndexService().getContentService(), document);
            try {
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to create embedding from document {}", document.getId());
            }
        });
        if (embedding.get() != null) {
            INDEX_METRICS.time("Index", (t) -> {
                try {
                } catch (Exception e) {
                    LOGGER.atError().setCause(e).log("Failed to index embedding for {}", document.getId());
                }
            });
        }
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return null;
    }

    @Override
    public float[] embed(org.springframework.ai.document.Document document) {
        return new float[0];
    }

    public void close() {
        if (indexer != null) indexer.release();
    }

    private void initIndex() {
        IndexerOptions options = (IndexerOptions) IndexerOptions.create(LuceneFields.INDEX_NAME)
                .analyzer(new StandardAnalyzer()).metrics(INDEX_METRICS)
                .tag("embedding").tag("lucene")
                .name("Embedding").description("An index for storing embeddings and their associated text segments")
                .build();
        indexer = indexService.createIndexer(options);
    }

    private void initEncodings() {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        encoding = registry.getEncoding(EncodingType.CL100K_BASE);
    }

    public LuceneContentRetriever getContentRetriever() {
        if (contentRetriever == null) {
            synchronized (this) {
                contentRetriever = new LuceneContentRetriever(this);
            }
        }
        return contentRetriever;
    }


    private Field toField(Map.Entry<String, Object> entry) {
        String fieldName = entry.getKey();
        var fieldValue = entry.getValue();
        Field field;
        if (fieldValue instanceof String string) {
            field = new StringField(fieldName, string, Field.Store.YES);
        } else if (fieldValue instanceof Integer number) {
            field = new IntField(fieldName, number, Field.Store.YES);
        } else if (fieldValue instanceof Long number) {
            field = new LongField(fieldName, number, Field.Store.YES);
        } else if (fieldValue instanceof Float number) {
            field = new FloatField(fieldName, number, Field.Store.YES);
        } else if (fieldValue instanceof Double number) {
            field = new DoubleField(fieldName, number, Field.Store.YES);
        } else {
            field = new StringField(fieldName, String.valueOf(fieldValue), Field.Store.YES);
        }
        return field;
    }
}
