package net.microfalx.bootstrap.web.controller.admin.database;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("DatabaseNodeController")
@RequestMapping(value = "/system/database/node")
@DataSet(model = Node.class)
@Help("admin/database/node")
public class NodeController extends SystemDataSetController<Node, String> {

    public NodeController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
