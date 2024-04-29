package net.microfalx.bootstrap.metrics;

import lombok.CustomLog;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.lang.ClassUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Service
@CustomLog
public class MetricsService extends ApplicationContextSupport implements InitializingBean {

    private final Collection<Repository> repositories = new CopyOnWriteArrayList<>();

    /**
     * Returns registered repositories.
     *
     * @return a non-null instance
     */
    public Collection<Repository> getRepositories() {
        return unmodifiableCollection(repositories);
    }

    /**
     * Executes a query with a repository and returns the results.
     * <p>
     * Each query targets a specific repository using {@link Query#getType()}. If no repository can be located, the
     * query will fail.
     *
     * @param query the query
     * @return a non-null instance
     * @see Repository
     */
    public Result query(Query query) {
        Repository repository = locateRepository(query);
        return repository.query(query);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeRepositories();
    }

    private Repository locateRepository(Query query) {
        requireNonNull(query);
        for (Repository repository : repositories) {
            if (repository.supports(query)) return repository;
        }
        throw new MetricException("A repository for query type '" + query.getType() + "' is not registered, query " + query);
    }

    private void initializeRepositories() {
        LOGGER.info("Loads repositories:");
        Collection<Repository> loadedRepositories = ClassUtils.resolveProviderInstances(Repository.class);
        for (Repository loadedRepository : loadedRepositories) {
            LOGGER.info(" - " + ClassUtils.getName(loadedRepository));
            if (loadedRepository instanceof ApplicationContextSupport applicationContextSupport) {
                applicationContextSupport.update(this);
            }
            repositories.add(loadedRepository);
        }
    }
}
