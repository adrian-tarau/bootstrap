package net.microfalx.bootstrap.support.metric;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support/metric/summary")
@DataSet(model = Summary.class)
@Help("admin/metric/summary")
public class SummaryController extends DataSetController<Summary, String> {

    public SummaryController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
