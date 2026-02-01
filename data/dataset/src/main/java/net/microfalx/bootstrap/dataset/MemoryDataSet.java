package net.microfalx.bootstrap.dataset;

import com.google.common.collect.Lists;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.lang.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.util.*;

import static java.time.Duration.ofSeconds;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A data set backed by data in memory, which holds cached data from various services.
 * <p>
 * Subclasses would implement {@link #extractModels()} to retrieve the models from services, store it with
 * {@link DataSetService} in internal caches and reused for any instance of the data set.
 */
public abstract class MemoryDataSet<M, F extends Field<M>, ID> extends AbstractDataSet<M, F, ID> {

    private Duration expiration = ofSeconds(30);

    public MemoryDataSet(DataSetFactory<M, F, ID> factory, Metadata<M, F, ID> metadata) {
        super(factory, metadata);
        setReadOnly(true);
    }

    /**
     * Returns the expiration time.
     *
     * @return a non-null instance
     */
    public Duration getExpiration() {
        return expiration;
    }

    /**
     * Changes the expiration.
     *
     * @param expiration the expiration
     * @return self
     */
    public MemoryDataSet<M, F, ID> setExpiration(Duration expiration) {
        requireNonNull(expiration);
        this.expiration = expiration;
        return this;
    }

    /**
     * Changes the expiration to expire "immediately".
     *
     * @return self
     */
    public MemoryDataSet<M, F, ID> expireImmediately() {
        this.expiration = Duration.ofMillis(500);
        return this;
    }

    @Override
    protected final List<M> doFindAll() {
        return getCachedModels(Filter.EMPTY).getModels();
    }

    @Override
    protected final List<M> doFindAllById(Iterable<ID> ids) {
        Map<ID, M> modelsById = getCachedModels(Filter.EMPTY).getModelsById();
        List<M> models = new ArrayList<>();
        for (ID id : ids) {
            M model = modelsById.get(id);
            if (model != null) models.add(model);
        }
        return models;
    }

    @Override
    protected final Optional<M> doFindById(ID id) {
        return Optional.ofNullable(getCachedModels(Filter.EMPTY).getModelsById().get(id));
    }

    @Override
    protected final boolean doExistsById(ID id) {
        return getCachedModels(Filter.EMPTY).getModelsById().containsKey(id);
    }

    @Override
    protected final long doCount() {
        return getCachedModels(Filter.EMPTY).getModels().size();
    }

    @Override
    protected final List<M> doFindAll(Sort sort) {
        List<M> models = getCachedModels(Filter.EMPTY).getModels();
        return sort(models, sort);
    }

    @Override
    protected final Page<M> doFindAll(Pageable pageable) {
        List<M> models = getCachedModels(Filter.EMPTY).getModels();
        return getPage(models, pageable);
    }

    @Override
    protected final Page<M> doFindAll(Pageable pageable, Filter filterable) {
        List<M> models = getCachedModels(filterable).getModels();
        return getPage(models, pageable, filterable);
    }

    @Override
    protected Optional<M> doFindByDisplayValue(String displayValue) {
        Metadata<M, F, ID> metadata = getMetadata();
        List<M> models = getCachedModels(Filter.EMPTY).getModels();
        for (M model : models) {
            String name = metadata.getName(model);
            if (ObjectUtils.equals(name, displayValue)) return Optional.of(model);
        }
        return Optional.empty();
    }

    /**
     * Returns the models associated with the data set.
     *
     * @return a non-null instance
     */
    private DataSetService.CachedModelsById<M, ID> getCachedModels(Filter filterable) {
        DataSetService dataSetService = getDataSetService();
        DataSetService.CachedModelsById<M, ID> cachedModels = dataSetService.getCacheById(getMetadata().getModel(), filterable);
        if (cachedModels == null) {
            Iterable<M> models = extractModels(filterable);
            if (models == null) models = Collections.emptyList();
            Map<ID, M> modelsMap = buildMap(models);
            List<M> modelsList = buildList(models);
            cachedModels = new DataSetService.CachedModelsById<>(getMetadata().getModel(), modelsList, modelsMap, getExpiration());
            dataSetService.registerCache(cachedModels, filterable);
        }
        return cachedModels;
    }

    /**
     * Extracts models to be cached and used with this data set.
     *
     * In most cases, the memory data sets are not filtered. However, in some cases, at least a time
     * filter is provided to restrict the number of results.
     *
     * @param filterable the filter received by the data set
     * @return the models (can be NULL)
     */
    protected abstract Iterable<M> extractModels(Filter filterable);

    private List<M> buildList(Iterable<M> models) {
        if (models instanceof List) {
            return (List<M>) models;
        } else {
            return Lists.newArrayList(models);
        }
    }

    private Map<ID, M> buildMap(Iterable<M> models) {
        Map<ID, M> modelsMap = new HashMap<>();
        for (M model : models) {
            ID id = getId(model);
            modelsMap.put(id, model);
        }
        return modelsMap;
    }


}
