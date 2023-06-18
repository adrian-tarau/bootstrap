package net.microfalx.bootstrap.security.group;

import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.dataset.annotation.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("security/groups")
@DataSet(model = Group.class)
public class GroupController extends DataSetController<Group, Integer> {

    @Autowired
    private GroupRepository userRepository;
}
