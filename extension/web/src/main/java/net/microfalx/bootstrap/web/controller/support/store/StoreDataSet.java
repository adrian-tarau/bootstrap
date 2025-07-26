package net.microfalx.bootstrap.web.controller.support.store;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.store.StoreService;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Provider
public class StoreDataSet extends PojoDataSet<Store, PojoField<Store>, String> {

    public StoreDataSet(DataSetFactory<Store, PojoField<Store>, String> factory, Metadata<Store, PojoField<Store>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<Store> doFindAll(Pageable pageable, Filter filterable) {
        List<Store> stores = getService(StoreService.class).getStores().stream().map(Store::from).collect(Collectors.toList());
        return getPage(stores, pageable, filterable);
    }
}
