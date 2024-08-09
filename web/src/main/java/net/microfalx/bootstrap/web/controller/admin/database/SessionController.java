package net.microfalx.bootstrap.web.controller.admin.database;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/system/database/session")
@DataSet(model = Session.class, defaultQuery = "state = 'Active'")
@Help("admin/database/session")
public class SessionController extends SystemDataSetController<Session, String> {
}
