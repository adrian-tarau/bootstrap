package net.microfalx.bootstrap.search;

import jakarta.annotation.PreDestroy;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.InfoStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.throwException;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Provides indexing capabilities for full text search.
 */
@Service
public class IndexService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    static Metrics METRICS = Metrics.of("Index");

    @Autowired
    private SearchProperties searchProperties;

    @Autowired
    private IndexProperties indexProperties;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ContentService contentService;

    private volatile AsyncTaskExecutor taskExecutor;

    private final Object lock = new Object();
    private volatile IndexHolder index;

    /**
     * Returns the executor used by the index service.
     *
     * @return a non-null instance
     */
    public AsyncTaskExecutor getTaskExecutor() {
        if (taskExecutor == null) initTaskExecutor();
        return taskExecutor;
    }

    /**
     * Indexes a document and commits at the end.
     *
     * @param document the document to index
     * @throws IndexException if the document cannot be indexed
     */
    public void index(Document document) {
        index(document, true);
    }

    /**
     * Indexes a document and commits at the end.
     *
     * @param document the document to index
     * @param commit   true - wait to commit in batched documents, false - commit immediately
     * @throws IndexException if the document cannot be indexed
     */
    public void index(Document document, boolean commit) {
        requireNonNull(document);
        index(Collections.singleton(document), commit);
    }

    /**
     * Indexes a collection of documents and commits at the end.
     *
     * @param documents the collections of documents to index
     * @throws IndexException if the document cannot be indexed
     */
    public void index(Collection<Document> documents) {
        index(documents, true);
    }

    /**
     * Returns a count with documents in the index.
     *
     * @return a positive integer
     */
    public long getDocumentCount() {
        IndexHolder indexHolder = openIndex();
        return indexHolder.indexWriter.getDocStats().numDocs;
    }

    /**
     * Returns a count with documents pending in memory.
     *
     * @return a positive integer
     */
    public int getPendingDocumentCount() {
        IndexHolder indexHolder = openIndex();
        return (int) (indexHolder.indexWriter.getPendingNumDocs() - getDocumentCount());
    }

    /**
     * Forces all documents in memory to be flushed on disk.
     */
    public void flush() {
        commitIndexWriter();
    }

    /**
     * Indexes a collection of documents.
     *
     * @param documents the collections of documents to index
     * @param commit    true - wait to commit in batched documents, false - commit immediately
     * @throws IndexException if the document cannot be indexed
     */
    public void index(Collection<Document> documents, boolean commit) {
        requireNonNull(documents);
        try {
            doWithIndex("Index", indexWriter -> {
                DocumentMapper itemMapper = new DocumentMapper(contentService);
                for (Document document : documents) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(" - index item, id: " + document.getId() + ", name: " + document.getName());
                    }
                    itemMapper.write(indexWriter, document);
                }
            });
        } catch (Exception e) {
            throw new IndexException("Failed to index collection", e);
        } finally {
            try {
                markIndexChanged(commit);
            } catch (Exception e) {
                LOGGER.error("Failed to commit index", e);
            }
        }
    }

    /**
     * Removes a document with from the index.
     * <p/>
     * Item removal is asynchronous, index will be committed later.
     *
     * @param itemId the id of the item to remove
     */
    public void remove(String itemId) {
        requireNonNull(itemId);
        doWithIndex("Delete", indexWriter -> {
            LOGGER.info("Deleting item with id: " + itemId);
            indexWriter.deleteDocuments(new Term(Document.ID_FIELD, itemId));
        });
    }

    /**
     * Clear the index.
     */
    public synchronized void clear() {
        commitAndRelease();
        try {
            FileUtils.deleteDirectory(getIndexDirectory());
        } catch (IOException e) {
            LOGGER.error("Failed to remove index " + getIndexDirectory(), e);
        }
    }

    /**
     * Commits any pending changes.
     */
    public void commit() {
        commitIndexWriter();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        openIndex();
        initTaskExecutor();
    }

    @PreDestroy
    protected void destroy() {
        commitIndexWriter();
        commitAndRelease();
    }

    /**
     * Returns the maximum memory used by RAM Directories.
     *
     * @return maximum memory in MB
     */
    private int getRAMBufferSizeMB() {
        int maxRamMB = (int) (Runtime.getRuntime().maxMemory() / 4) / (1024 * 1024);
        maxRamMB = Math.max(16, maxRamMB);
        maxRamMB = Math.min(100, maxRamMB);
        return indexProperties.getRamBufferSize() > 0 ? (int) indexProperties.getRamBufferSize() : maxRamMB;
    }

    /**
     * Returns the maximum memory used by RAM Directories per thread.
     *
     * @return maximum memory in MB
     */
    private int getRAMBPerThreadBufferSizeMB() {
        int ramBufferSizeMB = getRAMBufferSizeMB();
        return indexProperties.getRamBufferSizeThread() > 0 ? (int) indexProperties.getRamBufferSizeThread() : ramBufferSizeMB / 10;
    }

    /**
     * Opens the index, if not already opened.
     *
     * @return a non-null instance
     */
    protected synchronized IndexHolder openIndex() {
        if (index != null) return index;
        try {
            index = METRICS.time("Open", (Supplier<IndexHolder>) () -> createIndex(false));
            return index;
        } catch (Exception e) {
            if (index != null) index.release();
            return throwException(e);
        }
    }

    /**
     * Performs an operation on an index.
     *
     * @param indexCallback the index callback
     */
    protected void doWithIndex(String name, final IndexCallback indexCallback) {
        IndexHolder index = openIndex();
        try {
            METRICS.getTimer(name).record(() -> {
                try {
                    indexCallback.doWithIndex(index.indexWriter);
                } catch (Exception e) {
                    throwException(e);
                }
            });
        } catch (Exception throwable) {
            commitAndRelease();
            throwException(throwable);
        } finally {
            index.indexLastUpdated = System.currentTimeMillis();
            index.indexChanged.set(true);
        }
    }

    /**
     * Commits any pending documents and closes index structures.
     */
    private void commitAndRelease() {
        synchronized (lock) {
            LOGGER.debug("Rollback and release");
            if (index != null) index.release();
            index = null;
        }
    }

    /**
     * Marks the index changed if commit is false or commits the index.
     *
     * @param commit <code>true</code> to commit later, <code>false</code> to commit right away
     */
    private void markIndexChanged(boolean commit) {
        IndexHolder indexHolder = openIndex();
        if (commit) {
            commitIndexWriter();
        } else {
            indexHolder.indexChanged.set(true);
        }
        indexHolder.indexLastUpdated = System.currentTimeMillis();
    }

    /**
     * Commits the index.
     */
    void commitIndexWriter() {
        try {
            doWithIndex("Commit", indexWriter -> {
                LOGGER.debug("Committing index writer");
                indexWriter.flush();
                indexWriter.commit();
                index.indexChangedOptimizationPending.set(true);
            });
        } catch (Exception e) {
            LOGGER.error("Failed to commit changes to index", e);
        }
    }

    /**
     * Returns the directory which holds the index.
     *
     * @return a non-null instance
     */
    private File getIndexDirectory() {
        Resource resource = resourceService.getPersisted("index");
        return ((FileResource) resource).getFile();
    }

    /**
     * Creates the index.
     *
     * @param recreate {@code true} to recreate the index, {@code false} otherwise
     */
    private IndexHolder createIndex(boolean recreate) {
        IndexWriterConfig.OpenMode openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
        if (recreate) openMode = IndexWriterConfig.OpenMode.CREATE;

        IndexWriterConfig writerConfig = new IndexWriterConfig(Analyzers.createIndexAnalyzer());
        writerConfig.setOpenMode(openMode);
        writerConfig.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
        writerConfig.setMergeScheduler(new ConcurrentMergeScheduler());
        writerConfig.setUseCompoundFile(true);

        writerConfig.setRAMBufferSizeMB(getRAMBufferSizeMB());
        writerConfig.setRAMPerThreadHardLimitMB(getRAMBPerThreadBufferSizeMB());

        if (LOGGER.isDebugEnabled()) writerConfig.setInfoStream(new LoggerInfoStream());

        try {
            Directory directory = new NIOFSDirectory(getIndexDirectory().toPath());
            IndexWriter indexWriter = new IndexWriter(directory, writerConfig);
            if (openMode == IndexWriterConfig.OpenMode.CREATE) indexWriter.commit();
            LOGGER.debug("Create index writer, RAM Buffer " + writerConfig.getRAMBufferSizeMB() + " MB" +
                    ", Thread RAM Buffer " + writerConfig.getRAMPerThreadHardLimitMB() + " MB" +
                    ", Use compound files " + writerConfig.getUseCompoundFile());
            return new IndexHolder(indexWriter, directory);
        } catch (IOException e) {
            throw new IndexException("Failed to create the index", e);
        }
    }

    private void initTaskExecutor() {
        taskExecutor = TaskExecutorFactory.create().setSuffix("index").createExecutor();
    }

    private boolean isItemValid(Document document) {
        return isNotEmpty(document.getId());
    }

    static class IndexHolder {
        private final IndexWriter indexWriter;
        private final Directory directory;

        private volatile long indexLastUpdated;
        private volatile long indexLastFlushed = System.currentTimeMillis();
        private volatile long indexLastMerged = System.currentTimeMillis();
        private final AtomicBoolean indexChanged = new AtomicBoolean(false);
        private final AtomicBoolean indexChangedOptimizationPending = new AtomicBoolean(false);

        IndexHolder(IndexWriter indexWriter, Directory directory) {
            this.indexWriter = indexWriter;
            this.directory = directory;
        }

        void release() {
            try {
                indexWriter.flush();
                indexWriter.commit();
            } catch (Exception e) {
                LOGGER.error("Failed to commit index", e);
            }

            try {
                indexWriter.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close index", e);
            }

            try {
                directory.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close directory", e);
            }
        }
    }

    private static class LoggerInfoStream extends InfoStream {

        @Override
        public void message(String component, String message) {
            LOGGER.debug("[Info Stream] " + component + " - " + message);
        }

        @Override
        public boolean isEnabled(String component) {
            return true;
        }

        @Override
        public void close() {

        }
    }

    interface IndexCallback {

        void doWithIndex(IndexWriter indexWriter) throws Exception;
    }

}
