package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.web.application.Asset;
import net.microfalx.bootstrap.web.dashboard.DashboardController;
import net.microfalx.bootstrap.web.dashboard.annotation.Dashboard;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Dashboard("home")
@Controller
public final class HomeController extends DashboardController {

    @GetMapping("favicon.ico")
    public ResponseEntity<Object> favicon() {
        return AssetUtils.getResource("favicon.ico", Asset.Type.IMAGE);
    }

}
