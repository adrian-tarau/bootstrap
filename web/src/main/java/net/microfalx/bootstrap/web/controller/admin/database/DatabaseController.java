package net.microfalx.bootstrap.web.controller.admin.database;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/system/database")
@DataSet(model = Database.class)
@Help("admin/database")
public class DatabaseController extends DataSetController<Database, String> {
}
