package net.microfalx.bootstrap.serenity.task;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Open;
import org.openqa.selenium.By;

/**
 * A collection of interactions and question regarding the application login.
 */
public class Login {

    /**
     * Returns a task which logs ion the given user.
     *
     * @param userName the user
     * @param password the password
     * @return a non-null instance
     */
    public static Task as(String userName, String password) {
        return Application.task("{0} logs in as '" + userName + "'",
                Open.url(Application.getUri("login").toASCIIString()),
                new LoginPerformable(userName, password)
        );
    }

    private static class LoginPerformable implements Interaction {

        private final String userName;
        private final String password;

        public LoginPerformable(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        public <T extends Actor> void performAs(T actor) {
            actor.attemptsTo(Enter.theValue(userName).into(By.name("username")),
                    Enter.theValue(password).into(By.name("password")));
            actor.attemptsTo(Click.on(By.cssSelector("button[type='submit']")));
        }
    }
}
