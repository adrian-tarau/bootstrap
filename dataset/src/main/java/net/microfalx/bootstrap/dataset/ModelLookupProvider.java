package net.microfalx.bootstrap.dataset;

import com.google.common.collect.Iterables;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.lang.ArgumentUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public class ModelLookupProvider<M, ID> extends AbstractLookupProvider<Lookup<ID>, ID> {

    private final DataSet<M, Field<M>, ID> dataSet;

    public ModelLookupProvider(Class<Lookup<ID>> modelClass, DataSet<M, Field<M>, ID> dataSet) {
        super(modelClass);
        ArgumentUtils.requireNonNull(dataSet);
        this.dataSet = dataSet;
    }

    @Override
    protected Optional<Lookup<ID>> doFindById(ID id) {
        Optional<M> model = dataSet.findById(id);
        return model.map(this::convert);
    }

    @Override
    public Iterable<Lookup<ID>> doFindAll() {
        return Iterables.transform(dataSet.findAll(NAME_SORT), this::convert);
    }

    @Override
    public Page<Lookup<ID>> doFindAll(Pageable pageable, Filter filterable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), NAME_SORT);
        Page<M> page = dataSet.findAll(pageable, filterable);
        List<Lookup<ID>> converted = page.getContent().stream().map(this::convert).toList();
        return new DataSetPage<>(pageable, converted);
    }

    private Lookup<ID> convert(M model) {
        ID id = dataSet.getId(model);
        String name = dataSet.getName(model);
        return new DefaultLookup<>(id, name);
    }


}
