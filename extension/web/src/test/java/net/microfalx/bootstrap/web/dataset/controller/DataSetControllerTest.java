package net.microfalx.bootstrap.dataset.controller;

import net.microfalx.bootstrap.web.controller.AbstractControllerTestCase;
import net.microfalx.bootstrap.web.controller.HomeController;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {HomeController.class})
@WebMvcTest(HomeController.class)
class DataSetControllerTest extends AbstractControllerTestCase {

}