package net.microfalx.bootstrap.web.controller.admin.database;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/admin/database/list")
@DataSet(model = Database.class)
@Help("admin/database/list")
public class DatabaseController extends DataSetController<Database, String> {
}
