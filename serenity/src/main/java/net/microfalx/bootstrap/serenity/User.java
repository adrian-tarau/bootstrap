package net.microfalx.bootstrap.serenity;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.questions.Presence;
import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

/**
 * A collections of interactions and questions related to the security context (user).
 */
public class User {

    public static final Target USER_NAME_BUTTON = null;

    /**
     * Creates an interaction which validates whether the session is authentication.
     *
     * @return a non-null instance
     */
    public static Performable checkAuthenticated() {
        return Ensure.that("a valid security context", isAuthenticated()).isTrue();
    }

    /**
     * Returns a question which answers whether the session is authenticated, and it has the admin role.
     *
     * @return a non-null instance
     */
    public static Question<Boolean> isAdministrator() {
        return Question.about("user has ADMIN role").answeredBy(actor -> {
            if (isAuthenticated().answeredBy(actor)) {
                return false;
            }
            return false;
        });
    }

    /**
     * Returns a question which answers whether the session is authenticated.
     *
     * @return @{code} true if the user has a security context (is authenticated), {@code false} otherwise
     */
    public static Question<Boolean> isAuthenticated() {
        return Question.about("user authenticated").answeredBy(actor -> !isLoginPage(actor));
    }

    /**
     * Returns a question which answers whether the user has a given role.
     *
     * @return a non-null instance
     */
    public static Question<Boolean> hasRole(String role) {
        return Question.about("user has " + role + " role").answeredBy(actor -> false);
    }

    /**
     * Returns a question which answers whether the user has at least one of the given role.
     *
     * @return a non-null instance
     */
    public static Question<Boolean> hasAtLeastOneRole(String role) {
        return Question.about("user has at least " + role + " role").answeredBy(actor -> User.hasRole(role)).asBoolean();
    }

    private static boolean isLoginPage(Actor actor) {
        return Presence.of(By.name("username")).answeredBy(actor) &&
                Presence.of(By.name("password")).answeredBy(actor);
    }
}
