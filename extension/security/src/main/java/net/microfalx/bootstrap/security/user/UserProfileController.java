package net.microfalx.bootstrap.security.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.security.SecurityContext;
import net.microfalx.bootstrap.web.controller.PageController;
import net.microfalx.bootstrap.web.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("user/profile")
public class UserProfileController extends PageController {

    @Autowired private UserService userService;

    @GetMapping("")
    public String get(Model model) {
        User user = SecurityContext.get().getUser();
        model.addAttribute("user", new UserForm().setName(user.getDisplayName())
                .setEmail(user.getEmail()));
        return "security/user::#user-profile";
    }

    @PostMapping("")
    @ResponseBody
    public JsonResponse<?> save(@ModelAttribute UserForm userForm) {
        User user = SecurityContext.get().getUser();
        if (user.isExternal()) {
            return JsonResponse.fail("External user cannot have the profile changed");
        } else {
            userService.updateUser(userForm.getName(), userForm.getEmail());
            return JsonResponse.success().setPayload(userForm);
        }
    }

    @Getter
    @Setter
    @ToString
    public static class UserForm {

        @Size(min = 1, max = 100)
        private String name;
        @Size(min = 1, max = 100)
        private String email;
    }
}
