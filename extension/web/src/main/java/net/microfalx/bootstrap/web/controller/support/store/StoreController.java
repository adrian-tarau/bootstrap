package net.microfalx.bootstrap.web.controller.support.store;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support/store")
@DataSet(model = Store.class)
@Help("support/store")
public class StoreController extends SystemDataSetController<Store, String> {

    public StoreController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
