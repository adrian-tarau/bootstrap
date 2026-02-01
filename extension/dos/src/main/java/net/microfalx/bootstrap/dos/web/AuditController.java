package net.microfalx.bootstrap.dos.web;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.dos.jpa.Audit;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("DosAuditController")
@RequestMapping("/system/dos/audit")
@DataSet(model = Audit.class, trend = true, trendFieldNames = {"reason"})
@Help("admin/dos/audit")
public class AuditController extends DataSetController<Audit, Integer> {

    public AuditController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
