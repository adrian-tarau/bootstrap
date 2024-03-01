package net.microfalx.bootstrap.store;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;

import java.time.Duration;

/**
 * A class which can store items
 */
public interface Store<ID, T extends Identifiable<ID>> {

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
     *
     * @param item the item
     */
    void add(T item);

    /**
     * Removes an item from the store.
     *
     * @param id the item id
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
     * Counts the number of items in the store.
     *
     * @return the number of items in the store
     */
    int count();

    /**
     * Removes all items in the store.
     */
    void clear();

    /**
     * Options for store.
     */
    final class Options implements Identifiable<String>, Nameable {

        private final String id;
        private final String name;
        private Duration retention = Duration.ofDays(7);

        public static Options create(String name) {
            return new Options(name);
        }

        private Options(String name) {
            this.name = name;
            this.id = StringUtils.toIdentifier(name);
        }

        @Override
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Duration getRetention() {
            return retention;
        }
    }

}
