package net.microfalx.bootstrap.security.user.jpa;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.security.util.SecurityDataSetController;
import net.microfalx.bootstrap.web.util.JsonFormResponse;
import net.microfalx.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("security/users")
@DataSet(model = User.class, timeFilter = false)
@Help("admin/security/user")
public class UserController extends SecurityDataSetController<User, Integer> {

    @Autowired
    private UserRepository userRepository;

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
    protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<User, Field<User>, Integer> dataSet, User model, State state) {
        if (state == State.ADD) model.setPassword(passwordEncoder.encode(model.getPassword()));
    }
}
