package net.microfalx.bootstrap.web.controller.support.pool;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support/thread/task/scheduled")
@DataSet(model = ScheduledTask.class)
@Help("support/thread/task/scheduled")
public class ScheduledTaskController extends SystemDataSetController<ScheduledTask, Long>  {

    public ScheduledTaskController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
