package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public abstract class PojoDataSet<M, F extends PojoField<M>, ID> extends AbstractDataSet<M, F, ID> {

    public PojoDataSet(DataSetFactory<M, F, ID> factory, Metadata<M, F, ID> metadata) {
        super(factory, metadata);
    }

    /**
     * Filters and paginates a list of models.
     *
     * @param models   the models
     * @param pageable the page information
     * @param filter   the filter
     * @return a page of models
     */
    protected final Page<M> getPage(List<M> models, Pageable pageable, Filter filter) {
        ModelFilter<M> modelFilter = new ModelFilter<>(getMetadata(), models, filter);
        models = modelFilter.apply();
        ModelSorter<M> sorter = new ModelSorter<>(getMetadata(), models, DataSetUtils.from(pageable.getSort()));
        return new DataSetPage<>(pageable, sorter.apply());
    }

}
