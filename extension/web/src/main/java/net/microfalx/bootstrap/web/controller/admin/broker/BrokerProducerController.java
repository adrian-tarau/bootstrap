package net.microfalx.bootstrap.web.controller.admin.broker;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/system/broker/producer")
@DataSet(model = BrokerProducer.class)
@Help("admin/broker/producer")
public class BrokerProducerController extends SystemDataSetController<BrokerProducer, String> {
}
