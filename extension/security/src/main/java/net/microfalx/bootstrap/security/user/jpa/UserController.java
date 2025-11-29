package net.microfalx.bootstrap.security.user.jpa;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.mail.MailService;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.security.SecurityUtils;
import net.microfalx.bootstrap.security.user.UserService;
import net.microfalx.bootstrap.security.util.SecurityDataSetController;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Separator;
import net.microfalx.bootstrap.web.util.JsonFormResponse;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.StringUtils.replaceFirst;

@Controller
@RequestMapping("security/users")
@DataSet(model = User.class, timeFilter = false)
@Help("admin/security/user")
public class UserController extends SecurityDataSetController<User, Integer> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MetadataService metadataService;

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
        User user = userRepository.findByUserName(id);
            String password = SecurityUtils.getRandomPassword(12);
        userRepository.updatePassword(passwordEncoder.encode(password), true, id);
        String message = "Password was reset successfully.";
        if (isNotEmpty(user.getEmail())) {
            Resource body = getResetPasswordMessage(user, password);
            mailService.send(user.getEmail(), "Password Reset", body);
        } else {
            message += "<br><br>The user has no email address, please forward the new password:<br><i>" + password + "</i>";
        }
        return JsonResponse.success(message);
    }

    @PostMapping("{id}/generate_token")
    @ResponseBody
    public JsonResponse<?> generateToken(@PathVariable("id") String id) {
        if (!userService.exists(id)) throw new SecurityException("User with id " + id + " not found");
        String token = SecurityUtils.getRandomPassword(100);
        userRepository.updateToken(token, id);
        boolean showToken = ObjectUtils.equals(id, userService.getCurrentUser().getUserName());
        User user = userRepository.findByUserName(id);
        if (showToken) {
            return JsonResponse.success("Token generated successfully: " + token
                    + ". Make sure to store it securely as it won't be shown again.");
        } else {
            mailService.send(user.getEmail(), "REST API Key was changed",
                    MemoryResource.create(createAPIKeyEmail(user)));
        }
        return JsonResponse.success("Token generated successfully");
    }

    private String createAPIKeyEmail(User user) {
        try {
            String body = ClassPathResource.file("templates/security/api_key_email.txt").loadAsString();
            body = StringUtils.replaceFirst(body, "${USER]", user.getName());
            body = StringUtils.replaceFirst(body, "${API_KEY]", user.getToken());
            return body;
        } catch (IOException e) {
            return rethrowExceptionAndReturn(e);
        }
    }


    @Override
    protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<User, Field<User>, Integer> dataSet, User model, State state) {
        if (state == State.ADD) model.setPassword(passwordEncoder.encode(model.getPassword()));
    }

    private Resource getResetPasswordMessage(User user, String password) {
        try {
            Resource resource = ClassPathResource.file("templates/security/reset_password.txt");
            String text = replaceFirst(resource.loadAsString(), "${name}", user.getName());
            return MemoryResource.create(replaceFirst(text, "${password}", password));
        } catch (IOException e) {
            return rethrowExceptionAndReturn(e);
        }
    }
}
