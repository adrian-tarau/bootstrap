package net.microfalx.bootstrap.web.controller.admin.broker;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/system/broker/consumer")
@DataSet(model = BrokerConsumer.class)
@Help("admin/broker/consumer")
public class BrokerConsumerController extends DataSetController<BrokerConsumer, String> {
}
