package net.microfalx.bootstrap.store;

import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.rocksdb.RocksDbManager;
import org.rocksdb.RocksDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FormatterUtils.formatDuration;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A service responsible for managing a collection of {@link Store}.
 */
@Service
public class StoreService implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreService.class);

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private TaskScheduler taskScheduler;

    private final Map<String, Store<?, ?>> stores = new ConcurrentHashMap<>();

    /**
     * Registers a new store.
     *
     * @param options the options
     */
    public <T extends Identifiable<ID>, ID> Store<T, ID> registerStore(Store.Options options) {
        requireNonNull(options);
        LOGGER.info("Register store '{}', retention '{}'", options.getName(), formatDuration(options.getRetention()));
        Resource resource = resourceService.getPersisted("store");
        resource = resource.resolve(options.getId(), Resource.Type.DIRECTORY);
        StoreImpl<T, ID> store = new StoreImpl<>(options, resource);
        stores.put(options.getId(), store);
        return store;
    }

    /**
     * Returns a store with a given identifier.
     *
     * @param id   the store identifier
     * @param <ID> the identifier type
     * @param <T>  the item type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public <T extends Identifiable<ID>, ID> Store<T, ID> getStore(String id) {
        requireNonNull(id);
        Store store = stores.get(toIdentifier(id));
        if (store == null) throw new StoreException("A store with identifier '" + id + "' is not registered");
        return store;
    }

    /**
     * Returns registered stores.
     *
     * @return a non-null instance
     */
    public Collection<Store<?, ?>> getStores() {
        return Collections.unmodifiableCollection(stores.values());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        taskScheduler.scheduleAtFixedRate(new MaintenanceTask(), Duration.ofSeconds(5));
        taskScheduler.scheduleAtFixedRate(new CleanupTask(), Duration.ofHours(1));
        taskScheduler.scheduleAtFixedRate(new DiscoverTask(), Duration.ofMinutes(1));
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.info("Shutdown stores:");
        for (Store<?, ?> store : stores.values()) {
            LOGGER.info(" - " + store.getOptions().getName());
            try {
                ((StoreImpl<?, ?>) store).close();
            } catch (Exception e) {
                LOGGER.error("Failed to close store '" + store.getOptions().getName() + "'", e);
            }
        }
    }

    class DiscoverTask implements Runnable {

        private void register(File file, RocksDB rocksDB) {
            String id = "resource_" + Hashing.hash(file.getAbsolutePath());
            synchronized (stores) {
                if (!stores.containsKey(id)) {
                    LOGGER.info("Register resource store from " + file);
                    Store.Options options = Store.Options.create(id, "Resource " + StringUtils.capitalizeWords(file.getName()));
                    stores.put(id, new StoreImpl<>(options, FileResource.directory(file), rocksDB));
                }
            }
        }

        @Override
        public void run() {
            Collection<RocksDB> dbs = RocksDbManager.getInstance().list();
            for (RocksDB db : dbs) {
                File file = new File(db.getName());
                register(file, db);
            }
        }
    }

    class MaintenanceTask implements Runnable {

        @Override
        public void run() {
            for (Store<?, ?> store : stores.values()) {
                try {
                    store.flush();
                } catch (Exception e) {
                    LOGGER.error("Failed to flush store '" + store.getName() + "'");
                }
            }
        }
    }

    class CleanupTask implements Runnable {

        @Override
        public void run() {
            for (Store<?, ?> store : stores.values()) {
                try {
                    store.purge();
                } catch (Exception e) {
                    LOGGER.error("Failed to purge store '" + store.getName() + "'");
                }
            }
        }
    }
}
