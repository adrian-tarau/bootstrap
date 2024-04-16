package net.microfalx.bootstrap.store;

import net.microfalx.lang.*;
import net.microfalx.resource.Resource;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A class which can store arbitrary items in the local storage.
 */
public interface Store<T extends Identifiable<ID>, ID> extends Nameable, Iterable<T> {

    /**
     * Returns the directory used to store data.
     *
     * @return a non-null instance
     */
    Resource getDirectory();

    /**
     * Returns the store options.
     *
     * @return a non-null instance
     */
    Options getOptions();

    /**
     * Adds an item in the store.
     * <p>
     * If the item exists, it is updated.
     *
     * @param item the item
     */
    void add(T item);

    /**
     * Removes an item from the store.
     *
     * @param item the item
     */
    void remove(T item);

    /**
     * Removes an item from the store.
     *
     * @param id the item id
     */
    void remove(ID id);

    /**
     * Finds an item by id.
     *
     * @param id the item id
     * @return the item, null if such an item does not exist
     */
    T find(ID id);

    /**
     * Lists objects from the store, between a given timestamp and for a selected page.
     * <p>
     * If the objects stored in the store are not {@link Timestampable}, the time interval is ignored.
     *
     * @param query the query used to locate objects
     * @return a non-null instance
     */
    Collection<T> list(Query<T> query);

    /**
     * Lists objects from the store, between a given timestamp and for a selected page.
     * <p>
     * If the objects stored in the store are not {@link Timestampable}, the time interval is ignored.
     *
     * @param query the query used to locate objects
     */
    void walk(Query<T> query, Function<T, Boolean> callback);

    /**
     * Searches items from the store, between a given timestamp.
     * <p>
     * If the objects stored in the store are not {@link Timestampable}, the time interval is ignored.
     * <p>
     * If the callback returns {@code true}, the object was changed and it needs to be updated in the store, if
     * it returns {@code false} it needs to abort the loop, {@code null} it can continue and no update is made.
     *
     * @param query    the query used to locate objects
     * @param callback a callback used for each object matching the query
     */
    void update(Query<T> query, Function<T, Boolean> callback);

    /**
     * Counts the number of items in the store.
     *
     * @return the number of items in the store
     */
    long count();

    /**
     * Calculates the size of the store.
     *
     * @return the size in bytes
     */
    long size();

    /**
     * Removes all items in the store.
     */
    long clear();

    /**
     * Removes all items from the store.
     */
    void purge();

    /**
     * Options for store.
     */
    final class Options implements Identifiable<String>, Nameable, Cloneable {

        private final String id;
        private final String name;
        private Duration retention = Duration.ofDays(7);

        public static Options create(String name) {
            return new Options(StringUtils.toIdentifier(name), name);
        }

        public static Options create(String id, String name) {
            return new Options(id, name);
        }

        private Options(String id, String name) {
            requireNonNull(id);
            requireNonNull(name);
            this.id = toIdentifier(id);
            this.name = name;

        }

        @Override
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        /**
         * Return the retention of items in the store.
         *
         * @return a non-null instance
         */
        public Duration getRetention() {
            return retention;
        }

        /**
         * Changes the retention of the store.
         *
         * @param retention the retention
         * @return new instance
         */
        public Options withRetention(Duration retention) {
            requireNonNull(retention);
            Options copy = copy();
            copy.retention = retention;
            return copy;
        }

        private Options copy() {
            try {
                return (Options) clone();
            } catch (CloneNotSupportedException e) {
                return ExceptionUtils.throwException(e);
            }
        }
    }

}
