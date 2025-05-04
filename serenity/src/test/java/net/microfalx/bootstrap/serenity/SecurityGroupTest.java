package net.microfalx.bootstrap.serenity;

import net.microfalx.bootstrap.serenity.junit.AbstractDataSetSystemTestCase;
import net.microfalx.bootstrap.serenity.task.DataSet;
import net.microfalx.bootstrap.serenity.task.Form;
import net.microfalx.bootstrap.serenity.task.User;

public class SecurityGroupTest extends AbstractDataSetSystemTestCase {

    @Override
    protected User createUser() {
        return User.asAdmin();
    }

    @Override
    protected DataSet createDataSet() {
        return DataSet.create("security/groups", "Group");
    }

    @Override
    protected Form createAddForm() {
        Form form = Form.create();
        form.fieldByLabel("Name", "Serenity Test")
                .fieldByLabel("Enabled", true)
                .fieldByLabel("Roles", "Admin")
                .fieldByLabel("Description", "A security group registered by Serenity");
        return form;
    }

    @Override
    protected Form createEditForm() {
        return null;
    }
}
