package net.microfalx.bootstrap.web.controller.support.pool;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support/thread/task/completed")
@DataSet(model = CompletedTask.class)
@Help("support/thread/task/completed")
public class CompletedTaskController extends SystemDataSetController<CompletedTask, Long> {
}
