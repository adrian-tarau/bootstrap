package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.web.application.Asset;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication());
        return "index";
    }

    @GetMapping("favicon.ico")
    public ResponseEntity<Object> favicon() {
        return AssetUtils.getResource("favicon.ico", Asset.Type.IMAGE);
    }
}
