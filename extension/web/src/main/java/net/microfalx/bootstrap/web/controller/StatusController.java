package net.microfalx.bootstrap.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("ApplicationStatusController")
@RequestMapping(value = "/status")
public class StatusController implements AnonymousController {

    @GetMapping("")
    @ResponseBody
    public String get() {
        return "Ready!";
    }
}
