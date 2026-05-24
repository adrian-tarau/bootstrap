package net.microfalx.bootstrap.support.misc;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.support.store.Store;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support/library")
@DataSet(model = Library.class)
@Help("support/library")
public class LibraryController extends SystemDataSetController<Store, String> {

    public LibraryController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
