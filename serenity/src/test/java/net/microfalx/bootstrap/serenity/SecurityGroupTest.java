package net.microfalx.bootstrap.serenity;

import net.microfalx.bootstrap.serenity.task.DataSet;
import net.microfalx.bootstrap.serenity.task.Form;
import net.microfalx.bootstrap.serenity.task.Login;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecurityGroupTest extends AbstractSystemTestCase {

    private DataSet dataSet;

    @BeforeEach
    void setup() {
        toby.attemptsTo(Login.as(getAdminUserName(), getAdminPassword()));
        dataSet = DataSet.create("security/groups", "Group");
    }

    @Test
    void open() {
        toby.attemptsTo(dataSet.open());
    }

    @Test
    void add() {
        open();
        Form form = Form.create();
        form.fieldByLabel("Name", "Serenity Test")
                .fieldByLabel("Enabled", true)
                .fieldByLabel("Roles", "Admin")
                .fieldByLabel("Description", "A security group registered by Serenity");
        toby.attemptsTo(dataSet.add(form));
    }
}
