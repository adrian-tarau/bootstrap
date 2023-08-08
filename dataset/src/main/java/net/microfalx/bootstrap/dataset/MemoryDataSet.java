package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.*;
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
 * Subclasses would implement {@link  #extractModels()} to retrieve the models from services, store it with
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

    @Override
    protected final List<M> doFindAll() {
        return getCachedModels().getModels();
    }

    @Override
    protected final List<M> doFindAllById(Iterable<ID> ids) {
        Map<ID, M> modelsById = getCachedModels().getModelsById();
        List<M> models = new ArrayList<>();
        for (ID id : ids) {
            M model = modelsById.get(id);
            if (model != null) models.add(model);
        }
        return models;
    }

    @Override
    protected final Optional<M> doFindById(ID id) {
        return Optional.ofNullable(getCachedModels().getModelsById().get(id));
    }

    @Override
    protected final boolean doExistsById(ID id) {
        return getCachedModels().getModelsById().containsKey(id);
    }

    @Override
    protected final long doCount() {
        return getCachedModels().getModels().size();
    }

    @Override
    protected final List<M> doFindAll(Sort sort) {
        List<M> models = getCachedModels().getModels();
        ModelSorter<M> sorter = new ModelSorter<>(getMetadata(), models, DataSetUtils.from(sort));
        return sorter.apply();
    }

    @Override
    protected final Page<M> doFindAll(Pageable pageable) {
        List<M> models = getCachedModels().getModels();
        ModelSorter<M> sorter = new ModelSorter<>(getMetadata(), models, DataSetUtils.from(pageable.getSort()));
        return new DataSetPage<>(pageable, sorter.apply());
    }

    @Override
    protected final Page<M> doFindAll(Pageable pageable, Filter filterable) {
        List<M> models = getCachedModels().getModels();
        ModelFilter<M> filter = new ModelFilter<>(getMetadata(), models, filterable);
        models = filter.apply();
        ModelSorter<M> sorter = new ModelSorter<>(getMetadata(), models, DataSetUtils.from(pageable.getSort()));
        return new DataSetPage<>(pageable, sorter.apply());
    }


    /**
     * Returns the models associated with the data set.
     *
     * @return a non-null instance
     */
    private final DataSetService.CachedModels<M, ID> getCachedModels() {
        DataSetService dataSetService = getDataSetService();
        DataSetService.CachedModels<M, ID> cachedModels = dataSetService.getCached(getMetadata().getModel());
        if (cachedModels == null) {
            Collection<M> models = extractModels();
            if (models == null) models = Collections.emptyList();
            Map<ID, M> modelsMap = buildMap(models);
            List<M> modelsList = buildList(models);
            cachedModels = new DataSetService.CachedModels<>(getMetadata().getModel(), modelsList, modelsMap, getExpiration());
            dataSetService.registerCache(cachedModels);
        }
        return cachedModels;
    }

    /**
     * Extracts models to be cached and used with this data set.
     *
     * @return the models, can be NULL
     */
    protected abstract Collection<M> extractModels();

    private List<M> buildList(Collection<M> models) {
        if (models instanceof List) {
            return (List<M>) models;
        } else {
            return new ArrayList<>(models);
        }
    }

    private Map<ID, M> buildMap(Collection<M> models) {
        Map<ID, M> modelsMap = new HashMap<>();
        for (M model : models) {
            ID id = getId(model);
            modelsMap.put(id, model);
        }
        return modelsMap;
    }



}
