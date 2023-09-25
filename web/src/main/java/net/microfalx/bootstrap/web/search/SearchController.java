package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/search")
@DataSet(model = SearchResult.class, detailTemplate="search/browse")
public final class SearchController extends DataSetController<SearchResult, String> {

    @Autowired
    private SearchService searchService;

    @Override
    protected boolean beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<SearchResult, Field<SearchResult>, String> dataSet, Model controllerModel, SearchResult dataSetModel) {
        controllerModel.addAttribute("searchService", searchService);
        return true;
    }
}
