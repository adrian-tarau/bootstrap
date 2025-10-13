package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.dataset.formatter.FormatterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.ZoneId;

@RequestMapping("/settings")
@Controller
public class SettingsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);

    @PostMapping("session/time-zone")
    @ResponseBody()
    public void timeZone() {
        LOGGER.info("Change user time zone to '{}' from '{}'", FormatterUtils.getTimeZone(), ZoneId.systemDefault());
    }
}
