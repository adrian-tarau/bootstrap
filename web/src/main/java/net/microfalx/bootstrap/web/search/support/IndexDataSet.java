package net.microfalx.bootstrap.web.search.support;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Provider
public class IndexDataSet extends PojoDataSet<Index, PojoField<Index>, String> {

    private IndexService indexService;

    public IndexDataSet(DataSetFactory<Index, PojoField<Index>, String> factory, Metadata<Index, PojoField<Index>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        indexService = getService(IndexService.class);
    }

    @Override
    protected Page<Index> doFindAll(Pageable pageable, Filter filterable) {
        List<Index> indexes = indexService.getIndexers().stream().map(Index::from).toList();
        return getPage(indexes, pageable, filterable);
    }
}
