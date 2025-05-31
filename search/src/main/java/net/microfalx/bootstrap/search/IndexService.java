package net.microfalx.bootstrap.search;

import jakarta.annotation.PreDestroy;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ObjectUtils;
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
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.singleton;
import static net.microfalx.bootstrap.search.SearchUtils.INDEX_METRICS;
import static net.microfalx.bootstrap.search.SearchUtils.isLuceneException;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.throwException;

/**
 * Provides indexing capabilities for full text search.
 */
@Service
public class IndexService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    @Autowired(required = false)
    private SearchProperties searchProperties = new SearchProperties();

    @Autowired(required = false)
    private IndexProperties indexProperties = new IndexProperties();

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ContentService contentService;

    private volatile ThreadPool threadPool;

    private final Lock lock = new ReentrantLock();
    private final Collection<IndexListener> listeners = new CopyOnWriteArrayList<>();
    private final Queue<Collection<Document>> pendingDocuments = new ArrayBlockingQueue<>(500);
    private volatile Indexer indexer;

    /**
     * Returns the service used to access the content of the documents.
     * @return a non-null instance
     */
    public ContentService getContentService() {
        return contentService;
    }

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
     * Registers an index listener.
     *
     * @param listener the listener to register
     */
    public void registerListener(IndexListener listener) {
        requireNonNull(listener);
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            LOGGER.debug("Registered index listener {}", ClassUtils.getName(listener));
        } else {
            LOGGER.warn("Index listener already registered {}", ClassUtils.getName(listener));
        }
    }

    /**
     * Indexes a document and commits at the end.
     *
     * @param document the document to index
     * @throws IndexException if the document cannot be indexed
     */
    public void index(Document document) {
        index(document, false);
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
        index(singleton(document), commit);
    }

    /**
     * Indexes a collection of documents and commits at the end.
     *
     * @param documents the collections of documents to index
     * @throws IndexException if the document cannot be indexed
     */
    public void index(Collection<Document> documents) {
        index(documents, false);
    }

    /**
     * Returns a count with documents in the index.
     *
     * @return a positive integer
     */
    public long getDocumentCount() {
        return openIndexer().getDocumentCount();
    }

    /**
     * Returns a count with documents pending in memory.
     *
     * @return a positive integer
     */
    public int getPendingDocumentCount() {
        return openIndexer().getPendingDocumentCount();
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
                return null;
            });
            boolean isEmpty = pendingDocuments.isEmpty();
            pendingDocuments.offer(documents);
            if (isEmpty) getThreadPool().execute(new IndexedDocumentsTask());
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
            return indexWriter.deleteDocuments(new Term(Document.ID_FIELD, itemId));
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
            LOGGER.atError().setCause(e).log("Failed to remove index {}", getIndexDirectory());
        }
    }

    /**
     * Commits any pending changes.
     */
    public void commit() {
        commitIndex();
    }

    /**
     * Creates the index.
     *
     * @param directory the directory where the index is stored
     * @param options   the options for the indexer
     */
    public Indexer createIndexer(File directory, IndexerOptions options) {
        requireNonNull(directory);
        requireNonNull(options);
        IndexWriterConfig.OpenMode openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
        if (options.isRecreate()) openMode = IndexWriterConfig.OpenMode.CREATE;
        IndexWriterConfig writerConfig = new IndexWriterConfig(options.getAnalyzer());
        writerConfig.setOpenMode(openMode);
        writerConfig.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
        writerConfig.setMergeScheduler(new ConcurrentMergeScheduler());
        writerConfig.setUseCompoundFile(true);
        writerConfig.setRAMBufferSizeMB(getRAMBufferSizeMB());
        writerConfig.setRAMPerThreadHardLimitMB(getRAMBPerThreadBufferSizeMB());
        if (LOGGER.isDebugEnabled()) writerConfig.setInfoStream(new LoggerInfoStream());
        try {
            Directory luceneDirectory = new NIOFSDirectory(directory.toPath());
            IndexWriter indexWriter = new IndexWriter(luceneDirectory, writerConfig);
            if (openMode == IndexWriterConfig.OpenMode.CREATE) indexWriter.commit();
            LOGGER.debug("Create index writer, RAM Buffer " + writerConfig.getRAMBufferSizeMB() + " MB" +
                    ", Thread RAM Buffer " + writerConfig.getRAMPerThreadHardLimitMB() + " MB" +
                    ", Use compound files " + writerConfig.getUseCompoundFile());
            return new Indexer(indexWriter, luceneDirectory, options);
        } catch (IOException e) {
            throw new IndexException("Failed to create the index", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        openIndexer();
        initListeners();
        initTaskExecutor();
        initTasks();
        ThreadPool.get().scheduleAtFixedRate(new IndexMaintenanceTask(), Duration.ofSeconds(30));
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
     * Returns the index
     *
     * @return the index, null if not available
     */
    private Indexer getIndexer() {
        lock.lock();
        try {
            return indexer;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Opens the index, if not already opened.
     *
     * @return a non-null instance
     */
    private Indexer openIndexer() {
        lock.lock();
        try {
            if (indexer != null) return indexer;
            try {
                indexer = INDEX_METRICS.time("Open", () -> createIndexer(getIndexDirectory(), IndexerOptions.create()));
                return indexer;
            } catch (Exception e) {
                if (indexer != null) indexer.release();
                return throwException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Performs an operation on an index.
     *
     * @param name     the name of the operation
     * @param callback the index callback
     */
    private <R> R doWithIndex(String name, Indexer.Callback<R> callback) {
        try {
            RetryTemplate template = new RetryTemplate();
            return template.execute(context -> {
                Indexer index = openIndexer();
                return index.doWithIndex(name, callback);
            });
        } catch (Exception e) {
            if (isLuceneException(e)) releaseIndex();
            return throwException(e);
        }
    }

    /**
     * Commits any pending documents and closes index structures.
     */
    private void releaseIndex() {
        lock.lock();
        try {
            LOGGER.debug("Rollback and release");
            if (indexer != null) indexer.release();
            indexer = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Marks the index changed if commit is false or commits the index.
     *
     * @param commit <code>true</code> to commit later, <code>false</code> to commit right away
     */
    private void markIndexChanged(boolean commit) {
        Indexer currentIndexer = getIndexer();
        if (currentIndexer != null) currentIndexer.markIndexChanged(commit);
    }

    /**
     * Commits the index.
     */
    void commitIndex() {
        try {
            Indexer currentIndexer = getIndexer();
            if (currentIndexer != null) currentIndexer.commit();
        } catch (AlreadyClosedException e) {
            LOGGER.debug("Index is closed to commit changes to index", e);
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

    private void initListeners() {
        Collection<IndexListener> discoveredListeners = ClassUtils.resolveProviderInstances(IndexListener.class);
        LOGGER.info("Register {} index listeners", discoveredListeners.size());
        for (IndexListener discoveredListener : discoveredListeners) {
            LOGGER.debug(" - {}", ClassUtils.getName(discoveredListener));
            registerListener(discoveredListener);
        }
    }

    private void initTasks() {
        getThreadPool().scheduleAtFixedRate(new IndexedDocumentsTask(), 0, 10, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void initTaskExecutor() {
        threadPool = ThreadPoolFactory.create("Indexer").create();
    }

    class IndexMaintenanceTask implements Runnable {

        @Override
        public void run() {
            try {
                commitIndex();
            } catch (Exception e) {
                LOGGER.error("Failed to commit index", e);
            }
        }
    }

    class IndexedDocumentsTask implements Runnable {

        private void processDocuments(Collection<Document> documents) {
            LOGGER.debug("Fire indexed listener for {} documents", documents.size());
            for (IndexListener listener : listeners) {
                try {
                    listener.afterIndexing(documents);
                } catch (Exception e) {
                    LOGGER.atError().setCause(e).log("Failed to process indexed documents with listener {}",
                            ClassUtils.getName(listener));
                }
            }
        }

        @Override
        public void run() {
            while (!pendingDocuments.isEmpty()) {
                Collection<Document> documents = pendingDocuments.poll();
                if (ObjectUtils.isEmpty(documents)) break;
                processDocuments(documents);
            }
        }
    }

    private static class LoggerInfoStream extends InfoStream {

        @Override
        public void message(String component, String message) {
            LOGGER.debug("[Info Stream] {} - {}", component, message);
        }

        @Override
        public boolean isEnabled(String component) {
            return true;
        }

        @Override
        public void close() {
            // no resources to close
        }
    }

}
