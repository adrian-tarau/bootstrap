package net.microfalx.bootstrap.security.group;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("security/groups")
@DataSet(model = Group.class, timeFilter = false)
@Help("admin/security/group")
public class GroupController extends DataSetController<Group, Integer> {

    @Autowired
    private GroupRepository userRepository;
}
