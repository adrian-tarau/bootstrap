package net.microfalx.bootstrap.store;

import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FormatterUtils.formatDuration;

/**
 * A service responsible for managing a collection of {@link Store}.
 */
@Service
public class StoreService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreService.class);

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    private final Map<String, Store<?, ?>> stores = new ConcurrentHashMap<>();

    /**
     * Registers a new store.
     *
     * @param options the options
     */
    public <ID, T extends Identifiable<ID>> Store<ID, T> registerStore(Store.Options options) {
        requireNonNull(options);
        LOGGER.info("Register store '{}', retention '{}'", options.getName(), formatDuration(options.getRetention()));
        Resource resource = resourceService.getPersisted("store");
        resource = resource.resolve(options.getId(), Resource.Type.DIRECTORY);
        StoreImpl<ID, T> store = new StoreImpl<>(options, resource);
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
    public <ID, T extends Identifiable<ID>> Store<ID, T> getStore(String id) {
        requireNonNull(id);
        Store store = stores.get(StringUtils.toIdentifier(id));
        if (store == null) throw new StoreException("A store with identifier '" + id + "' is not registered");
        return store;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
