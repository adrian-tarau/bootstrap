package net.microfalx.bootstrap.web.search.support;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support/search/index")
@DataSet(model = Index.class, viewClasses = "modal-xl")
@Help("support/search/index")
public class IndexController extends DataSetController<Index, String> {

    public IndexController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
