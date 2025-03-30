package net.microfalx.bootstrap.serenity;

import net.microfalx.bootstrap.serenity.task.Application;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Open;

/**
 * An interaction regarding the application logout
 */
public class Logout {

    /**
     * Return a task that logout a user
     *
     * @return a non-null instance
     */
    public static Task logout() {
        return Task.where("{0} logout",
                Open.url(Application.getUri("logout").toASCIIString()),
                Application.checkLogin());
    }
}
