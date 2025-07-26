package net.microfalx.bootstrap.web.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public abstract class UnderConstructionController extends PageController {

    @GetMapping()
    public final String view(Model model) {
        model.addAttribute("title", getTitle());
        return "under_construction";
    }
}
