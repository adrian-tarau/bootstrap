package net.microfalx.bootstrap.security.user;

import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.dataset.annotation.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("security/users")
@DataSet(model = User.class)
public class UserController extends DataSetController<User, Integer> {

    @Autowired
    private UserRepository userRepository;
}
