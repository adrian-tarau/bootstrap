package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.web.dataset.AbstractDataSet;
import net.microfalx.bootstrap.web.dataset.DataSetFactory;
import net.microfalx.bootstrap.web.dataset.PojoDataSet;
import net.microfalx.bootstrap.web.dataset.PojoDataSetFactory;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

public class SearchDataSet extends PojoDataSet<SearchResult, PojoField<SearchResult>, String> {

    public SearchDataSet(DataSetFactory<SearchResult, PojoField<SearchResult>, String> factory, Metadata<SearchResult, PojoField<SearchResult>> metadata) {
        super(factory, metadata);
    }

    public static class Factory extends PojoDataSetFactory<SearchResult, PojoField<SearchResult>, String> {

        @Override
        protected AbstractDataSet<SearchResult, PojoField<SearchResult>, String> doCreate(Metadata<SearchResult, PojoField<SearchResult>> metadata) {
            return new SearchDataSet(this, metadata);
        }

        @Override
        public boolean supports(Metadata<SearchResult, PojoField<SearchResult>> metadata) {
            return isAssignable(metadata.getModel(), SearchResult.class);
        }
    }
}