package net.microfalx.bootstrap.web.controller.admin.metric;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/admin/metric/timer")
@DataSet(model = Timer.class)
@Help("admin/metric/timer")
public class TimerController extends DataSetController<Timer, String> {
}
