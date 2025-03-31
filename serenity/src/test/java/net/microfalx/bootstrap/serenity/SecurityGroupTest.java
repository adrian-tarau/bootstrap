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
        Form form = Form.create();
        form.fieldByLabel("Name", "aaa")
                .fieldByLabel("Description", "bbb");
        toby.attemptsTo(dataSet.add(form));
    }
}
