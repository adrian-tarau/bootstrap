package net.microfalx.bootstrap.security.audit;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.security.SecurityDataSetController;
import net.microfalx.bootstrap.security.audit.jpa.Audit;
import net.microfalx.bootstrap.security.audit.jpa.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("security/audit")
@DataSet(model = Audit.class, trend = true, trendFieldNames = {"action", "module", "category", "client_info"})
@Help("admin/security/audit")
public class AuditController extends SecurityDataSetController<Audit, Integer> {

    @Autowired
    private AuditRepository auditRepository;
}
