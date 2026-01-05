package net.microfalx.bootstrap.web.controller.admin.restapi;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("RestApiAuditAdminController")
@RequestMapping(value = "/system/restapi/audit")
@DataSet(model = Audit.class)
@Help("admin/restapi/audit")
public class AuditController extends DataSetController<Audit, Integer> {

    public AuditController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
