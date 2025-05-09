package net.microfalx.bootstrap.search;

import jakarta.annotation.PreDestroy;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import net.microfalx.threadpool.ThreadPool;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.InfoStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static net.microfalx.bootstrap.search.SearchUtils.INDEX_METRICS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.throwException;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Provides indexing capabilities for full text search.
 */
@Service
public class IndexService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    private SearchProperties searchProperties;

    @Autowired
    private IndexProperties indexProperties;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ContentService contentService;

    private volatile ThreadPool threadPool;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rlock = lock.readLock();
    private final Lock wlock = lock.writeLock();
    private volatile IndexHolder index;

    /**
     * Returns the thread pool used by the index service.
     *
     * @return a non-null instance
     */
    public ThreadPool getThreadPool() {
        if (threadPool == null) initTaskExecutor();
        return threadPool;
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
        commitIndex();
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
        releaseIndex();
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
        commitIndex();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        openIndex();
        initTaskExecutor();
    }

    @PreDestroy
    protected void destroy() {
        commitIndex();
        releaseIndex();
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
    protected IndexHolder openIndex() {
        wlock.lock();
        try {
            if (index != null) return index;
            try {
                index = INDEX_METRICS.time("Open", () -> createIndex(false));
                return index;
            } catch (Exception e) {
                if (index != null) index.release();
                return throwException(e);
            }
        } finally {
            wlock.unlock();
        }
    }

    /**
     * Performs an operation on an index.
     *
     * @param indexCallback the index callback
     */
    protected void doWithIndex(String name, final IndexCallback indexCallback) {
        try {
            RetryTemplate template = new RetryTemplate();
            template.registerListener(new RetryListener() {
                @Override
                public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    releaseIndex();
                }
            });
            template.execute(context -> {
                IndexHolder index = openIndex();
                INDEX_METRICS.getTimer(name).record(() -> {
                    try {
                        indexCallback.doWithIndex(index.indexWriter);
                    } catch (Exception e) {
                        throwException(e);
                    }
                });
                return null;
            });
        } catch (Exception throwable) {
            releaseIndex();
            throwException(throwable);
        } finally {
            markIndexChanged(false);
        }


    }

    /**
     * Commits any pending documents and closes index structures.
     */
    private void releaseIndex() {
        wlock.lock();
        try {
            LOGGER.debug("Rollback and release");
            if (index != null && index.indexWriter.isOpen()) {
                index.commit();
                index.release();
            }
            index = null;
        } finally {
            wlock.unlock();
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
            commitIndex();
            indexHolder.indexChanged.set(false);
        } else {
            indexHolder.indexChanged.set(true);
        }
        indexHolder.indexLastUpdated = System.currentTimeMillis();
    }

    /**
     * Commits the index.
     */
    void commitIndex() {
        try {
            doWithIndex("Commit", indexWriter -> {
                LOGGER.debug("Committing index writer");
                if (indexWriter.isOpen()) {
                    indexWriter.flush();
                    indexWriter.commit();
                }
                index.indexChangedOptimizationPending.set(true);
            });
        } catch (AlreadyClosedException e) {
            LOGGER.error("Index closedFailed to commit changes to index", e);
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
        threadPool = ThreadPoolFactory.create("Indexer").create();
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

        void commit() {
            try {
                if (indexWriter.isOpen()) {
                    indexWriter.flush();
                    indexWriter.commit();
                }
                indexChangedOptimizationPending.set(true);
            } catch (Exception e) {
                LOGGER.error("Failed to commit index", e);
            }
        }

        void release() {
            try {
                if (indexWriter.isOpen()) indexWriter.close();
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
