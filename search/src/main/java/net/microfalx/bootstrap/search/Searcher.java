package net.microfalx.bootstrap.search;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static net.microfalx.bootstrap.search.SearchUtils.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.throwException;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * Represents a searcher in the search system.
 */
public class Searcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Searcher.class);

    private final SearcherOptions options;
    private final IndexSearcher indexSearcher;
    private final IndexReader indexReader;
    private final Directory directory;

    private final AtomicBoolean open = new AtomicBoolean();
    private final AtomicInteger useCount = new AtomicInteger(0);
    private final long created = System.currentTimeMillis();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rlock = lock.readLock();
    private final Lock wlock = lock.writeLock();

    Searcher(File directory, SearcherOptions options) throws IOException {
        requireNonNull(directory);
        requireNonNull(options);
        this.options = options;
        this.directory = new NIOFSDirectory(directory.toPath(), NativeFSLockFactory.getDefault());
        indexReader = SEARCH_METRICS.timeCallable("Open Reader", () -> DirectoryReader.open(this.directory));
        indexSearcher = SEARCH_METRICS.timeCallable("Open Searcher", () -> new IndexSearcher(this.indexReader, options.getThreadPool()));
    }

    /**
     * Returns the searcher options.
     *
     * @return a non-null instance
     */
    public SearcherOptions getOptions() {
        return options;
    }

    /**
     * Returns the Lucene index reader.
     *
     * @return a non-null instance
     */
    public IndexReader getReader() {
        rlock.lock();
        try {
            return indexReader;
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Returns the Lucene index searcher.
     *
     * @return a non-null instance
     */
    public IndexSearcher getSearcher() {
        rlock.lock();
        try {
            return indexSearcher;
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Performs an operation on an index.
     *
     * @param name     the action name
     * @param callback the index callback
     */
    public <R> R doWithSearcher(String name, Searcher.Callback<R> callback) {
        rlock.lock();
        try {
            if (!isOpen()) throw new IndexException("Index is closed");
            RetryTemplate template = new RetryTemplate();
            return template.execute(context -> INDEX_METRICS.getTimer(name).recordCallable(() -> callback.doWithSearcher(indexSearcher)));
        } catch (Exception e) {
            if (isLuceneException(e)) release();
            return throwException(e);
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Returns whether the searcher is opened.
     *
     * @return {@code true} if the searcher is open, {@code false} otherwise
     */
    public boolean isOpen() {
        return open.get();
    }

    /**
     * Releases the searcher and closes the underlying index reader and directory.
     */
    public void release() {
        wlock.lock();
        try {
            open.set(false);
            SEARCH_METRICS.time("Release", (t) -> {
                try {
                    indexReader.close();
                } catch (Exception e) {
                    LOGGER.error("Failed to close index reader", e);
                }
                try {
                    if (directory != null) directory.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to rollback index", e);
                }
            });
        } finally {
            wlock.unlock();
        }
    }

    /**
     * Returns whether the searcher is stale and needs to be refreshed.
     *
     * @return {@code true} if the searcher is stale, {@code false} otherwise
     */
    public boolean isStale() {
        return millisSince(created) > options.getRefreshInterval().toMillis();
    }

    /**
     * A callback interface for searches on the index.
     *
     * @param <R> the return type of the operation
     */
    public interface Callback<R> {

        /**
         * Performs a search on the index.
         *
         * @param indexSearcher the index searcher
         * @return the result of the operation
         * @throws Exception if an error occurs during the operation
         */
        R doWithSearcher(IndexSearcher indexSearcher) throws Exception;
    }
}
