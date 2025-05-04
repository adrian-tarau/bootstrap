package net.microfalx.bootstrap.serenity.task;

import lombok.Getter;
import lombok.ToString;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.questions.JavaScript;
import net.serenitybdd.screenplay.targets.Target;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A collections of interactions and questions related to the security context (user).
 */
@ToString
public class User implements Interaction {

    public static final Target USER_NAME_BUTTON = null;

    @Getter
    private final String userName;
    @Getter
    private final String password;

    public static User asAdmin() {
        return create(getAdminUserName(), getAdminPassword());
    }

    public static User asRegular() {
        return create(getRegularUserName(), getRegularPassword());
    }

    public static User asGuest() {
        return create(getGuestUserName(), getGuestPassword());
    }

    public static User create(final String userName, final String password) {
        return new User(userName, password);
    }

    private User(String userName, String password) {
        requireNonNull(userName);
        this.userName = userName;
        this.password = password;
    }

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
            if (isAuthenticated().answeredBy(actor)) return hasRole("admin").answeredBy(actor);
            return false;
        });
    }

    /**
     * Returns a question which answers whether the session is authenticated.
     *
     * @return @{code} true if the user has a security context (is authenticated), {@code false} otherwise
     */
    public static Question<Boolean> isAuthenticated() {
        return Question.about("user authenticated").answeredBy(actor -> JavaScript.evaluate("return User.isAuthenticated()").viewedBy(actor).asBoolean());
    }

    /**
     * Returns a question which answers whether the user has a given role.
     *
     * @return a non-null instance
     */
    public static Question<Boolean> hasRole(String role) {
        return Question.about("user has " + role + " role").answeredBy(actor -> JavaScript.evaluate("return User.hasRole()").viewedBy(actor).asBoolean());
    }

    /**
     * Returns a question which answers whether the user has at least one of the given role.
     *
     * @return a non-null instance
     */
    public static Question<Boolean> hasAtLeastOneRole(String role) {
        return Question.about("user has at least " + role + " role").answeredBy(actor -> User.hasRole(role)).asBoolean();
    }

    /**
     * Returns an interaction which logs in with the current user.
     *
     * @return self
     */
    public Interaction login() {
        return this;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(Login.as(userName, password));
    }

    private static String getAdminUserName() {
        return "test_admin";
    }

    private static String getAdminPassword() {
        return System.getProperty("test.security.admin.password", "test123");
    }

    private static String getRegularUserName() {
        return "test_user";
    }

    private static String getRegularPassword() {
        return System.getProperty("test.security.user.password", "test123");
    }

    private static String getGuestUserName() {
        return "test_guest";
    }

    private static String getGuestPassword() {
        return System.getProperty("test.security.guest.password", "test123");
    }
}
