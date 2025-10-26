package net.microfalx.bootstrap.security.user.jpa;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        menu.add(new Item().setAction("user.generate_token").setText("Generate token")
                .setIcon("fa-solid fa-key").setDescription("Generates a new API key for the user"));
    }

    @PostMapping("{id}/generate_token")
    public JsonResponse<?> generateToken(@PathVariable("id") String id) {
        if (!userService.exists(id)) throw new SecurityException("User with id " + id + " not found");
        String token = SecurityUtils.getRandomPassword(100);
        userRepository.updateToken(token, id);
        return JsonResponse.success("Token generated successfully: " + token
                + ". Make sure to store it securely as it won't be shown again.");
    }


    @Override
    protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<User, Field<User>, Integer> dataSet, User model, State state) {
        if (state == State.ADD) model.setPassword(passwordEncoder.encode(model.getPassword()));
    }
}
