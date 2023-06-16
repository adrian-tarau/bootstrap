package net.microfalx.bootstrap.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/search")
public class SearchController {

    @GetMapping()
    public String home(Model model) {
        return "search/index";
    }
}
