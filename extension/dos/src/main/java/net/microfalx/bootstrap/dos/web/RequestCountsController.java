package net.microfalx.bootstrap.dos.web;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("DosRequestController")
@RequestMapping("/system/dos/request")
@DataSet(model = RequestCounts.class, timeFilter = false)
@Help("admin/dos/rule")
public class RequestCountsController extends DataSetController<RequestCounts, Integer> {

    public RequestCountsController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
