package net.microfalx.bootstrap.web.controller.support.pool;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support/thread/pool")
@DataSet(model = ThreadPool.class)
@Help("support/thread/pool")
public class ThreadPoolController extends SystemDataSetController<ThreadPool, String> {

    public ThreadPoolController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
