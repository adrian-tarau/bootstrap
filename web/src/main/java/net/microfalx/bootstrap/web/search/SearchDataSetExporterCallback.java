package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.dataset.AbstractDataSetExportCallback;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.annotation.Provider;

@Provider
public class SearchDataSetExporterCallback extends AbstractDataSetExportCallback<SearchResult, Field<SearchResult>, String> {

    @Override
    public boolean isExportable(DataSet<SearchResult, Field<SearchResult>, String> dataSet, Field<SearchResult> field, boolean exportable) {
        return exportable || "body".equals(field.getName());
    }

}
