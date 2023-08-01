package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.dataset.DataSetController;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/search")
@DataSet(model = SearchResult.class)
public final class SearchController extends DataSetController<SearchResult, String> {

    @Autowired
    private SearchService searchService;

}
