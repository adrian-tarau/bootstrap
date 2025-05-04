package net.microfalx.bootstrap.serenity;

import net.microfalx.bootstrap.serenity.junit.AbstractDataSetSystemTestCase;
import net.microfalx.bootstrap.serenity.task.DataSet;
import net.microfalx.bootstrap.serenity.task.Form;
import net.microfalx.bootstrap.serenity.task.User;

public class SecurityUserTest extends AbstractDataSetSystemTestCase {

    @Override
    protected User createUser() {
        return User.asAdmin();
    }

    @Override
    protected DataSet createDataSet() {
        return DataSet.create("security/users", "Users");
    }

    @Override
    protected Form createAddForm() {
        Form form = Form.create();
        return form;
    }

    @Override
    protected Form createEditForm() {
        Form form = Form.create();
        return form;
    }
}
