package net.microfalx.bootstrap.security.user;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.security.provisioning.SecurityProperties;
import net.microfalx.bootstrap.web.controller.PageController;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

@RequestMapping("/")
@Controller
public class LoginController extends PageController {

    @Autowired
    private SecurityProperties properties;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/login")
    public String homePage(Model model, @RequestParam(value = "error", required = false) String error) {
        updateModel(model);
        Throwable exception = (Throwable) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (error != null || exception != null) {
            model.addAttribute(MESSAGE_ATTR, "Invalid user name (email) or password");
        }
        return "security/login";
    }

    @PostMapping(path = "/login/auth")
    public String authRedirect(Model model) {
        return REDIRECT_HOME;
    }

    @PostMapping(path = "/logout", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String logout(Model model) {
        return HOME;
    }

    @GetMapping("/login/register")
    public String registerPage(Model model) {
        updateModel(model);
        if (properties.isRegister()) {
            return "security/register";
        } else {
            return REDIRECT_LOGIN;
        }
    }

    @PostMapping(path = "/login/register", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String registerAccount(Model model, AccountInfo accountInfo) {
        updateModel(model);
        if (properties.isRegister()) {
            requireNotEmpty(accountInfo.getName());
            requireNotEmpty(accountInfo.getEmail());
            requireNotEmpty(accountInfo.getPassword());
            String email = accountInfo.getEmail();
            String userName = StringUtils.split(email, "@")[0];
            userService.register(accountInfo.getName(), userName, accountInfo.getPassword(), accountInfo.getEmail());
        }
        return REDIRECT_LOGIN;
    }

    private void updateModel(Model model) {
        model.addAttribute("settings", properties);
    }

    @Getter
    @Setter
    @ToString
    public static class AuthInfo {

        private String username;
        private String password;
        private boolean rememberMe;
    }


    @Getter
    @Setter
    @ToString
    public static class AccountInfo {

        private String name;
        private String email;
        private String password;
        private boolean terms;
    }
}
