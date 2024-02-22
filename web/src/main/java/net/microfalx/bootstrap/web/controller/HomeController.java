package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.dataset.formatter.FormatterUtils;
import net.microfalx.bootstrap.web.application.Asset;
import net.microfalx.bootstrap.web.dashboard.DashboardController;
import net.microfalx.bootstrap.web.dashboard.annotation.Dashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/")
@Dashboard("home")
@Controller
public final class HomeController extends DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("favicon.ico")
    public ResponseEntity<Object> favicon() {
        return AssetUtils.getResource("favicon.ico", Asset.Type.IMAGE);
    }

    @PostMapping("time-zone")
    @ResponseBody()
    public void timeZone() {
        LOGGER.debug("Change time zone to " + FormatterUtils.getTimeZone());
    }

}
