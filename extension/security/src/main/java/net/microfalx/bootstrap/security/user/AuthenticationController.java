package net.microfalx.bootstrap.security.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.bootstrap.web.controller.PageController;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("auth")
@Controller
public class AuthenticationController extends PageController {

    @GetMapping()
    public String execute(Model model) {
        return "login";
    }

    @PostMapping(value = "login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String authenticate(Model model, @RequestParam("username") String userName, @RequestParam("password") String password,
                               RedirectAttributes redirectAttributes) {
        return REDIRECT_HOME;
    }

    @GetMapping("logout")
    public final String execute(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) new SecurityContextLogoutHandler().logout(request, response, authentication);
        return REDIRECT_HOME;
    }
}
