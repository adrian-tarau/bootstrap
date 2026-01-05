package net.microfalx.bootstrap.security.user.jpa;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.mail.MailService;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.security.SecurityUtils;
import net.microfalx.bootstrap.security.user.UserService;
import net.microfalx.bootstrap.security.util.SecurityDataSetController;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Separator;
import net.microfalx.bootstrap.web.util.JsonFormResponse;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.StringUtils.replaceFirst;

@Controller
@RequestMapping("security/users")
@DataSet(model = User.class, timeFilter = false)
@Help("admin/security/user")
public class UserController extends SecurityDataSetController<User, Integer> {

    private final UserService userService;
    private final MailService mailService;

    public UserController(DataSetService dataSetService, UserService userService, MailService mailService) {
        super(dataSetService);
        this.userService = userService;
        this.mailService = mailService;
    }

    @Override
    protected void validate(User model, State state, JsonFormResponse<?> response) {
        super.validate(model, state, response);
        model.setUserName(model.getUserName().toLowerCase());
        if (state == State.ADD) {
            if (!ObjectUtils.equals(model.getPassword(), model.getRetypePassword())) {
                response.addError("password", "Passwords must be the same", false);
                response.addError("retypePassword", "Passwords must be the same", false);
            }
        }
    }

    @Override
    protected void updateActions(Menu menu) {
        super.updateActions(menu);
        menu.add(new Separator());
        menu.add(new Item().setAction("user.reset_password").setText("Reset password")
                .setIcon("fa-solid fa-key").setDescription("Generates a new password for the user"));
        menu.add(new Item().setAction("user.generate_token").setText("Generate token")
                .setIcon("fa-solid fa-key").setDescription("Generates a new API key for the user"));
    }

    @PostMapping("{id}/reset_password")
    @ResponseBody
    public JsonResponse<?> resetPassword(@PathVariable("id") String id) {
        if (!userService.exists(id)) throw new SecurityException("User with id " + id + " not found");
        UserRepository userRepository = userService.getUserRepository();
        User user = userRepository.findByUserName(id);
            String password = SecurityUtils.getRandomPassword(12);
        userRepository.updatePassword(userService.getPasswordEncoder().encode(password), true, id);
        boolean showToken = ObjectUtils.equals(id, userService.getCurrentUser().getUserName());
        String message = "Password was updated successfully.";
        if (showToken) {
            message += "Make sure to store it securely as it won't be shown again. You will be forced to changed it on next login.";
        }
        if (isNotEmpty(user.getEmail())) {
            mailService.send(user.getEmail(), "Password Reset", createPasswordChangeEmail(user, password));
        } else {
            showToken = true;
            message += "<br><br>The user has no email address, please forward the new password";
        }
        JsonResponse<?> response = JsonResponse.success(message);
        if (showToken) response.addAttribute("password", password);
        return response;
    }

    @PostMapping("{id}/generate_token")
    @ResponseBody
    public JsonResponse<?> generateToken(@PathVariable("id") String id) {
        if (!userService.exists(id)) throw new SecurityException("User with id " + id + " not found");
        String token = SecurityUtils.getRandomPassword(60);
        UserRepository userRepository = userService.getUserRepository();
        userRepository.updateToken(token, id);
        boolean showToken = ObjectUtils.equals(id, userService.getCurrentUser().getUserName());
        User user = userRepository.findByUserName(id);
        String message = "Token was updated successfully.";
        if (showToken) {
            message += "Make sure to store it securely as it won't be shown again.";
        }
        if (isNotEmpty(user.getEmail())) {
            mailService.send(user.getEmail(), "API Key Reset", createAPIKeyChangeEmail(user, token));
        } else {
            showToken = true;
            message += "<br><br>The user has no email address, please forward the new token";
        }
        JsonResponse<?> response = JsonResponse.success(message);
        if (showToken) response.addAttribute("apiKey", token);
        return response;
    }


    @Override
    protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<User, Field<User>, Integer> dataSet, User model, State state) {
        if (state == State.ADD) model.setPassword(userService.getPasswordEncoder().encode(model.getPassword()));
    }

    private Resource createAPIKeyChangeEmail(User user, String token) {
        try {
            String body = ClassPathResource.file("templates/security/api_key_email.txt").loadAsString();
            body = replaceFirst(body, "${USER}", user.getName());
            body = replaceFirst(body, "${API_KEY}", token);
            return MemoryResource.create(body);
        } catch (IOException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    private Resource createPasswordChangeEmail(User user, String password) {
        try {
            String body = ClassPathResource.file("templates/security/reset_password.txt").loadAsString();
            body = replaceFirst(body, "${USER}", user.getName());
            body = replaceFirst(body, "${PASSWORD}", password);
            return MemoryResource.create(body);
        } catch (IOException e) {
            return rethrowExceptionAndReturn(e);
        }
    }
}
