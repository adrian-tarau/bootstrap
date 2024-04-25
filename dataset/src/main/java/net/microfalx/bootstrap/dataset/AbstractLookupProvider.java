package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.lang.ClassUtils;
import net.microfalx.metrics.Metrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ClassUtils.getClassParametrizedType;

/**
 * Base class for all lookup providers.
 *
 * @param <M> the model type
 */
public abstract class AbstractLookupProvider<M extends Lookup<ID>, ID> implements LookupProvider<M, ID> {

    static Metrics METRICS = DataSetUtils.METRICS.withGroup("Lookup");
    protected static Sort NAME_SORT = Sort.by("name");

    private final Class<M> modelClass;
    private volatile SoftReference<Iterable<M>> cachedModels = new SoftReference<>(null);
    private volatile SoftReference<Map<ID, M>> cachedModelsById = new SoftReference<>(null);
    DataSetService dataSetService;

    public AbstractLookupProvider() {
        this.modelClass = getClassParametrizedType(getClass(), 0);
    }

    public AbstractLookupProvider(Class<M> modelClass) {
        requireNonNull(modelClass);
        this.modelClass = modelClass;
    }

    @Override
    public final Class<M> getModel() {
        return modelClass;
    }

    @Override
    public final Optional<M> findById(ID id) {
        requireNonNull(id);
        return METRICS.time("Find By Id", () -> doFindById(id));
    }

    @Override
    public final Iterable<M> findAll() {
        return METRICS.time("Find All", this::internalDoFindAll);
    }

    @Override
    public final Iterable<M> findAll(Pageable pageable) {
        requireNonNull(pageable);
        return findAll(pageable, Filter.create());
    }

    @Override
    public final Page<M> findAll(Pageable pageable, Filter filterable) {
        requireNonNull(pageable);
        requireNonNull(filterable);
        return METRICS.time("Find All", () -> doFindAll(pageable, filterable));
    }

    protected Optional<M> doFindById(ID id) {
        requireNonNull(id);
        if (cachedModelsById.get() == null) internalDoFindAll();
        Map<ID, M> modelsById = cachedModelsById.get();
        return modelsById != null ? Optional.ofNullable(modelsById.get(id)) : Optional.empty();
    }

    protected final Iterable<M> internalDoFindAll() {
        if (cachedModels.get() == null) {
            Iterable<M> models = doFindAll();
            Metadata<M, Field<M>, ID> metadata = dataSetService.getMetadata(modelClass);
            models = DataSetUtils.sort(metadata, models, NAME_SORT);
            cachedModels = new SoftReference<>(models);
            Map<ID, M> modelsById = new HashMap<>();
            for (M model : models) {
                modelsById.put(model.getId(), model);
            }
            cachedModelsById = new SoftReference<>(modelsById);
        }
        return cachedModels.get();
    }

    protected Iterable<M> doFindAll(Pageable pageable) {
        return findAll(pageable, Filter.create());
    }

    protected Page<M> doFindAll(Pageable pageable, Filter filterable) {
        Iterable<M> models = findAll();
        Metadata<M, Field<M>, ID> metadata = dataSetService.getMetadata(modelClass);
        return DataSetUtils.getPage(metadata, models, pageable, filterable);
    }

    protected abstract Iterable<M> doFindAll();

    /**
     * Returns the data set service supporting this provider.
     *
     * @return a non-null instance
     */
    protected DataSetService getDataSetService() {
        if (dataSetService == null) {
            throw new DataSetException("Lookup provider '" + ClassUtils.getName(this) + "' was not initialized");
        }
        return dataSetService;
    }
}
