package net.microfalx.bootstrap.dataset;

import com.google.common.collect.Lists;
import net.microfalx.bootstrap.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static net.microfalx.lang.ClassUtils.getClassParametrizedType;

/**
 * Base class for all lookup providers.
 *
 * @param <M> the model type
 */
public abstract class AbstractLookupProvider<M> implements LookupProvider<M> {

    private final Class<M> modelClass;
    DataSetService dataSetService;

    public AbstractLookupProvider() {
        this.modelClass = getClassParametrizedType(getClass(), 0);
    }

    @Override
    public final Class<M> getModel() {
        return modelClass;
    }

    @Override
    public Page<M> extract(Pageable pageable, Filter filterable) {
        List<M> models = Lists.newArrayList(extractAll());
        Metadata<M, Field<M>, Object> metadata = dataSetService.getMetadata(modelClass);
        ModelFilter<M> filter = new ModelFilter<>(metadata, models, filterable);
        models = filter.apply();
        ModelSorter<M> sorter = new ModelSorter<>(metadata, models, DataSetUtils.from(pageable.getSort()));
        return new DataSetPage<>(pageable, sorter.apply());
    }
}
