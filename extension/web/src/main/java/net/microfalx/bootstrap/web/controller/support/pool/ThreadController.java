package net.microfalx.bootstrap.web.controller.support.pool;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support/thread")
@DataSet(model = Thread.class, defaultQuery = "state = 'Runnable'")
@Help("support/thread")
public class ThreadController extends SystemDataSetController<Thread, Long> {
}
