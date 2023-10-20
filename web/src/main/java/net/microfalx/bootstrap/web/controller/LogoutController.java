package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.web.dashboard.annotation.Dashboard;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("logout")
@Dashboard("home")
@Controller
public class LogoutController extends PageController {

    @GetMapping()
    public final String execute(Model model) {
        return HOME;
    }
}
