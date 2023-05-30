package net.microfalx.bootstrap.web.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping(value = "/")
    public String home(Model model) {
        model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication());
        return "index";
    }
}
