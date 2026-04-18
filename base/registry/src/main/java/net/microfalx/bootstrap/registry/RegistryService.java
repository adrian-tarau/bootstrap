package net.microfalx.bootstrap.registry;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ObjectUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.unmodifiableList;

@Service
@Slf4j
public class RegistryService implements InitializingBean {

    private final List<Storage> storages;

    private volatile Storage storage;
    private Registry registry;

    public RegistryService(List<Storage> storages) {
        if (ObjectUtils.isEmpty(storages)) storages = List.of(new MemoryStorage());
        this.storages = storages;
    }

    /**
     * Returns the current registry.
     *
     * @return a non-null instance
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Returns the registered storages.
     *
     * @return a non-null instance
     */
    public List<Storage> getStorages() {
        return unmodifiableList(storages);
    }

    /**
     * Returns the current storage.
     *
     * @return a non-null instance
     */
    public Storage getStorage() {
        if (storage == null) selectStorage();
        return storage;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registry = new RegistryImpl(this);
    }

    private synchronized void selectStorage() {
        if (storage == null) {
            for (Storage currentStorage : storages) {
                if (!currentStorage.isEnabled()) {
                    LOGGER.info("Registry storage {} is disabled, skip it", ClassUtils.getName(currentStorage));
                    continue;
                }
                storage = currentStorage;
                break;
            }
            LOGGER.info("Use registry storage {}", ClassUtils.getName(storage));
        }
    }
}
