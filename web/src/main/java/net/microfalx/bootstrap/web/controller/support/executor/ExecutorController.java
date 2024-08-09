package net.microfalx.bootstrap.web.controller.support.executor;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support/executor")
@DataSet(model = Executor.class)
@Help("support/executor")
public class ExecutorController extends SystemDataSetController<Executor, String> {
}
