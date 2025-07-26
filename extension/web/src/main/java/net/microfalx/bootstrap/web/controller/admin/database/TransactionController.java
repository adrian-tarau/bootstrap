package net.microfalx.bootstrap.web.controller.admin.database;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/system/database/transaction")
@DataSet(model = Transaction.class, defaultQuery = "state = 'Running'")
@Help("admin/database/transaction")
public class TransactionController extends SystemDataSetController<Transaction, String> {
}
