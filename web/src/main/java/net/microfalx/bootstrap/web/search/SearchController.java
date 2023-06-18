package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.dataset.annotation.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/search")
@DataSet(model = SearchResult.class)
public final class SearchController extends DataSetController<SearchResult, String> {

    @Autowired
    private SearchService searchService;

    @GetMapping()
    public String browse(Model model) {
        super.browse(model);
        return "search/index";
    }
}
