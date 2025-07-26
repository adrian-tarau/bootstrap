package net.microfalx.bootstrap.web.dataset;

import net.microfalx.bootstrap.web.controller.PageController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;

/**
 * Base class for all forms.
 */
public abstract class FormController<M, ID> extends PageController {

    private static final String VIEW = "dataset/form";

    @GetMapping()
    public final String view(Model model) {
        model.addAttribute("form", new HashMap<>());
        return VIEW;
    }
}
