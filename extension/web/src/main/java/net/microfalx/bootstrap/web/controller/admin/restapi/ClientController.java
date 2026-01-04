package net.microfalx.bootstrap.web.controller.admin.restapi;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("RestApiClientAdminController")
@RequestMapping(value = "/system/restapi/client")
@DataSet(model = Client.class, timeFilter = false)
@Help("admin/restapi/client")
public class ClientController extends DataSetController<Client, Integer> {
}
