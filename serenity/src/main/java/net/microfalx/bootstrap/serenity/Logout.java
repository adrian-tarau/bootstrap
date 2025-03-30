package net.microfalx.bootstrap.serenity;

import net.microfalx.bootstrap.serenity.task.Application;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import org.openqa.selenium.By;

/**
 * An interaction regarding the application logout
 */
public class Logout {

    /**
     * Return a task that logout a user
     *
     * @return a non-null instance
     */
    public static Task withLink() {
        return Task.where("{0} logout",
                Application.open("logout"),
                Application.checkLogin());
    }

    /**
     * Return a task that logout a user
     *
     * @return a non-null instance
     */
    public static Task withButton() {
        return Task.where("{0} logout",
                Click.on(By.id("user")),
                Click.on(By.linkText("Sign out")));
    }
}
