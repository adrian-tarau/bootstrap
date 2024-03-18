package net.microfalx.bootstrap.web.controller.admin.database;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/system/database/node")
@DataSet(model = Node.class)
@Help("admin/database/node")
public class NodeController extends DataSetController<Node, String> {
}
