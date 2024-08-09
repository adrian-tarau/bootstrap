package net.microfalx.bootstrap.security.group;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.security.SecurityDataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("security/groups")
@DataSet(model = Group.class, timeFilter = false)
@Help("admin/security/group")
public class GroupController extends SecurityDataSetController<Group, Integer> {

    @Autowired
    private GroupRepository userRepository;

    @Autowired
    private GroupService groupService;

    @Override
    protected boolean beforeEdit(net.microfalx.bootstrap.dataset.DataSet<Group, Field<Group>, Integer> dataSet, Model controllerModel, Group dataSetModel) {
        dataSetModel.setRoles(groupService.getRoles(dataSetModel));
        return super.beforeEdit(dataSet, controllerModel, dataSetModel);
    }

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<Group, Field<Group>, Integer> dataSet, Model controllerModel, Group dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) {
            dataSetModel.setRoles(groupService.getRoles(dataSetModel));
        }
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Group, Field<Group>, Integer> dataSet, Group model, State state) {
        super.afterPersist(dataSet, model, state);
        groupService.setRoles(model, model.getRoles());
    }

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Group, Field<Group>, Integer> dataSet, Group model, State state) {
        return super.beforePersist(dataSet, model, state);
    }
}
