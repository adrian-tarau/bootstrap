package net.microfalx.bootstrap.serenity;

import net.microfalx.bootstrap.serenity.junit.AbstractDataSetSystemTestCase;
import net.microfalx.bootstrap.serenity.task.DataSet;
import net.microfalx.bootstrap.serenity.task.User;

public class SecurityAuditTest extends AbstractDataSetSystemTestCase {

    @Override
    protected User createUser() {
        return User.asAdmin();
    }

    @Override
    protected DataSet createDataSet() {
        return DataSet.create("security/audit", "Audit").setCrud(false);
    }

}
