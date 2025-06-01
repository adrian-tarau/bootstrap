package net.microfalx.bootstrap.search;

import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.TimeUtils;
import net.microfalx.metrics.Metrics;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.bootstrap.search.SearchUtils.isLuceneException;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.throwException;
import static net.microfalx.lang.TimeUtils.FIVE_MINUTE;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * Represents an index in the search system.
 * <p>
 * This class wraps Lucene structures supporting the indexer.
 */
public class Indexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);

    private final static int MAX_INDEX_RETRIES = 3;

    private final IndexWriter indexWriter;
    private final Directory directory;
    private final IndexerOptions options;
    private final Metrics metrics;

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Lock rlock = lock.readLock();
    private final Lock wlock = lock.writeLock();

    private volatile long indexLastUpdated = currentTimeMillis();
    private volatile long indexLastCommited = currentTimeMillis();
    private volatile long indexLastMerged = currentTimeMillis();
    private volatile long indexSizeLastUpdated = TimeUtils.oneHourAgo();
    private volatile long indexMemorySize = -1;
    private volatile long indexDiskSize = -1;
    private final AtomicBoolean open = new AtomicBoolean(true);
    private final AtomicBoolean indexChanged = new AtomicBoolean(false);
    private final AtomicBoolean indexChangedOptimizationPending = new AtomicBoolean(false);
    private final AtomicInteger indexRetries = new AtomicInteger(MAX_INDEX_RETRIES);

    Indexer(IndexWriter indexWriter, Directory directory, IndexerOptions options) {
        requireNonNull(indexWriter);
        requireNonNull(directory);
        requireNonNull(directory);
        this.indexWriter = indexWriter;
        this.directory = directory;
        this.options = options;
        this.metrics = options.getMetrics();
    }

    /**
     * Returns the indexer options.
     *
     * @return a non-null instance
     */
    public IndexerOptions getOptions() {
        return options;
    }

    /**
     * Returns the Lucene index writer.
     *
     * @return a non-null instance
     */
    public IndexWriter getWriter() {
        return indexWriter;
    }

    /**
     * Returns the Lucene directory where the index is stored.
     *
     * @return a non-null instance
     */
    public Directory getDirectory() {
        return directory;
    }

    /**
     * Returns whether the index is opened
     *
     * @return {@code true} if the index is open, {@code false} otherwise
     */
    public boolean isOpen() {
        return open.get() && indexWriter.isOpen();
    }

    /**
     * Returns a count with documents in the index.
     *
     * @return a positive integer
     */
    public long getDocumentCount() {
        return indexWriter.getDocStats().numDocs;
    }

    /**
     * Returns a count with documents pending in memory.
     *
     * @return a positive integer
     */
    public int getPendingDocumentCount() {
        return (int) (indexWriter.getPendingNumDocs() - getDocumentCount());
    }

    /**
     * Returns the size of the index on disk in bytes
     *
     * @return a positive integer
     */
    public long getMemorySize() {
        updateSize();
        return indexMemorySize;
    }

    /**
     * Returns the size of the index on disk in bytes
     *
     * @return a positive integer
     */
    public long getDiskSize() {
        updateSize();
        return indexDiskSize;
    }

    /**
     * Commits pending changes to the index.
     */
    public void commit() {
        rlock.lock();
        try {
            if (!isOpen()) {
                LOGGER.info("Index is closed, cannot commit changes");
            } else {
                doWithIndex("Commit", indexWriter -> {
                    LOGGER.debug("Committing index writer");
                    if (indexWriter.isOpen()) {
                        indexWriter.flush();
                        indexWriter.commit();
                    }
                    return null;
                });
                indexLastCommited = currentTimeMillis();
            }
        } catch (Exception e) {
            if (isLuceneException(e)) {
                LOGGER.warn("Failed to commit changes to index. root cause: {}", ExceptionUtils.getRootCauseMessage(e));
            } else {
                ExceptionUtils.throwException(e);
            }
        } finally {
            rlock.unlock();
        }
        try {
            indexCommited();
        } catch (Exception e) {
            LOGGER.error("Failed to commit index", e);
        }
    }

    /**
     * Commits pending changes to the index, closes the index writer and releases resources.
     */
    public void release() {
        commit();
        open.set(false);
        wlock.lock();
        try {
            try {
                if (isOpen()) indexWriter.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close index", e);
            }
            try {
                directory.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close directory", e);
            }
        } finally {
            wlock.unlock();
        }
    }

    /**
     * Performs an operation on an index.
     *
     * @param name     the action name
     * @param callback the index callback
     */
    public <R> R doWithIndex(String name, Callback<R> callback) {
        boolean shouldRelease = false;
        rlock.lock();
        try {
            if (!isOpen()) throw new IndexException("Index is closed");
            RetryTemplate template = new RetryTemplate();
            return template.execute(context -> metrics.getTimer(name).recordCallable(() -> callback.doWithIndex(indexWriter)));
        } catch (Exception e) {
            shouldRelease = isLuceneException(e);
            return throwException(e);
        } finally {
            markIndexChanged(false);
            rlock.unlock();
            if (shouldRelease && indexRetries.decrementAndGet() <= 0) release();
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Indexer indexer)) return false;
        return Objects.equals(options, indexer.options);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(options);
    }

    void markIndexChanged(boolean commit) {
        if (commit) {
            commit();
        } else {
            indexChanged();
        }
    }

    protected void indexChanged() {
        indexChanged.set(true);
        indexLastUpdated = currentTimeMillis();
    }

    protected void indexCommited() {
        indexChanged.set(false);
        indexLastUpdated = currentTimeMillis();
        indexChangedOptimizationPending.set(true);
    }

    private void updateSize() {
        if (indexDiskSize == -1 || millisSince(indexSizeLastUpdated) > FIVE_MINUTE) {
            indexSizeLastUpdated = currentTimeMillis();
            indexDiskSize = FileUtils.sizeOfDirectory(options.getDirectory());
        }
    }

    /**
     * A callback interface for operations on the index.
     *
     * @param <R> the return type of the operation
     */
    public interface Callback<R> {

        /**
         * Invoked to perform an operation on the index under a retry loop.
         *
         * @param indexWriter the Lucene index writer
         * @return the result of the operation
         * @throws Exception the exception that may be thrown during the operation
         */
        R doWithIndex(IndexWriter indexWriter) throws Exception;
    }
}
