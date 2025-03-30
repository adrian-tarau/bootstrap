package net.microfalx.bootstrap.serenity;

import net.microfalx.bootstrap.serenity.task.Login;
import org.junit.jupiter.api.Test;

public class AuthenticationTest extends AbstractSystemTestCase {

    @Test
    void loginAsAdmin() {
        toby.attemptsTo(Login.as(getAdminUserName(), getAdminPassword()));
    }

    @Test
    void loginAsAdminAndLogout() {
        loginAsAdmin();
    }

    @Test
    void loginAsGuest() {
        toby.attemptsTo(Login.as(getGuestUserName(), getGuestPassword()));
    }
}
