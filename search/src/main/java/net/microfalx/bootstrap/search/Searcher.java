package net.microfalx.bootstrap.search;

import net.microfalx.metrics.Metrics;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static net.microfalx.bootstrap.search.SearchUtils.INDEX_METRICS;
import static net.microfalx.bootstrap.search.SearchUtils.isLuceneException;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.throwException;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * Represents a searcher in the search system.
 */
public class Searcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Searcher.class);

    private final SearcherOptions options;
    private volatile IndexSearcher indexSearcher;
    private volatile IndexReader indexReader;
    private final Directory directory;
    private final Metrics metrics;

    private final AtomicBoolean open = new AtomicBoolean(false);
    private final AtomicInteger useCount = new AtomicInteger(0);
    private final long created = System.currentTimeMillis();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rlock = lock.readLock();
    private final Lock wlock = lock.writeLock();

    Searcher(File directory, SearcherOptions options) throws IOException {
        requireNonNull(directory);
        requireNonNull(options);
        this.options = options;
        this.metrics = options.getMetrics();
        this.directory = new NIOFSDirectory(directory.toPath(), NativeFSLockFactory.getDefault());
        openReader();
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
     * Returns the Lucene stored fields.
     *
     * @return a non-null instance
     */
    public StoredFields getStoredFields() {
        rlock.lock();
        try {
            return indexReader.storedFields();
        } catch (IOException e) {
            throw new SearchException("Failed to retrieve stored fields", e);
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Returns the Lucene document for the given document ID.
     *
     * @param docID the document ID
     * @return a non-null instance of {@link Document}
     */
    public Document getDocument(int docID) {
        rlock.lock();
        try {
            return getStoredFields().document(docID);
        } catch (IOException e) {
            throw new SearchException("Failed to retrieve document with ID " + docID, e);
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Returns the Lucene document for the given document ID.
     *
     * @param docID      the document ID
     * @param fieldNames the set of field names to retrieve
     * @return a non-null instance of {@link Document}
     */
    public Document getDocument(int docID, Set<String> fieldNames) {
        rlock.lock();
        try {
            return getStoredFields().document(docID, fieldNames);
        } catch (IOException e) {
            throw new SearchException("Failed to retrieve document with ID " + docID, e);
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
        boolean shouldClose = false;
        try {
            if (isOpen()) {
                RetryTemplate template = new RetryTemplate();
                return template.execute(context -> INDEX_METRICS.getTimer(name).recordCallable(() -> callback.doWithSearcher(indexSearcher)));
            } else {
                throw new SearchException("Searcher is not open");
            }
        } catch (Exception e) {
            shouldClose = isLuceneException(e);
            return throwException(e);
        } finally {
            rlock.unlock();
            if (shouldClose) release();
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
            metrics.time("Release", (t) -> {
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Searcher searcher)) return false;
        return Objects.equals(options, searcher.options);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(options);
    }

    private void openReader() {
        indexReader = metrics.timeCallable("Open Reader", () -> DirectoryReader.open(this.directory));
        indexSearcher = metrics.timeCallable("Open Searcher", () -> new IndexSearcher(this.indexReader, options.getThreadPool()));
        open.set(true);
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
