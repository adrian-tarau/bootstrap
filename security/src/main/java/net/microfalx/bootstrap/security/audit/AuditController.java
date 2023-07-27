package net.microfalx.bootstrap.security.audit;

import net.microfalx.bootstrap.web.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.controller.DataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("security/audit")
@DataSet(model = Audit.class)
public class AuditController extends DataSetController<Audit, Integer> {

    @Autowired
    private AuditRepository auditRepository;
}
