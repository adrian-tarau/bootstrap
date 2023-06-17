package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.web.dataset.*;

public class SearchDataSet extends PojoDataSet<SearchResult, String> {

    public SearchDataSet(DataSetFactory<SearchResult, String> factory, Class<SearchResult> modelClass) {
        super(factory, modelClass);
    }

    public static class Factory extends PojoDataSetFactory<SearchResult, String> {

        @Override
        public boolean supports(Class<SearchResult> modelClass) {
            return SearchResult.class.equals(modelClass);
        }

        @Override
        public Expression parse(String value) {
            return null;
        }

        @Override
        public DataSet<SearchResult, String> create(Class<SearchResult> modelClass) {
            return new SearchDataSet(this, modelClass);
        }
    }
}
