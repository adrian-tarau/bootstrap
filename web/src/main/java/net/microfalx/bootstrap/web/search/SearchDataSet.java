package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;

@Provider
public class SearchDataSet extends PojoDataSet<SearchResult, PojoField<SearchResult>, String> {

    public SearchDataSet(DataSetFactory<SearchResult, PojoField<SearchResult>, String> factory, Metadata<SearchResult, PojoField<SearchResult>> metadata) {
        super(factory, metadata);
    }

}
