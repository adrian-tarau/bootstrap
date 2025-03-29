package net.microfalx.bootstrap.serenity;

import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.ThreadUtils;
import net.microfalx.lang.UriUtils;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.*;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.questions.Presence;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.hamcrest.core.IsAnything;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A collections of interactions and questions related to the application.
 */
public class Application {

    private static Duration timeout = Duration.ofSeconds(5);
    private static URI uri = URI.create("http://localhost:8080");

    /**
     * Returns the home page of the application
     *
     * @return a non-null instance
     */
    public static URI getUri() {
        return uri;
    }

    /**
     * Changes the home page of the application.
     *
     * @param uri the new home page
     */
    public static void setUri(URI uri) {
        requireNonNull(uri);
        Application.uri = uri;
    }

    /**
     * Returns sn URI for an application resource.
     *
     * @param path the path
     * @return a non-null instance
     */
    public static URI getUri(String path) {
        ArgumentUtils.requireNonNull(path);
        return UriUtils.appendPath(uri, path);
    }

    /**
     * Returns an interaction which will wait until the application is ready.
     *
     * @return a non-null instance
     */
    public static Interaction waitUntilReady() {
        String script = "return Application.isReady()";
        return WaitUntil.the(ExpectedConditions.jsReturnsValue(script));
    }

    /**
     * Returns an interaction which will wait until all pending AJAX requests are completed.
     *
     * @return a non-null instance
     */
    public static Interaction waitForPendingRequests() {
        return null;
    }

    /**
     * Creates an application task which has all the validation performed after a series of steps
     *
     * @param title the title of the task
     * @param steps the steps
     * @param <T>   the type of steps
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T extends Performable> Task task(String title, T... steps) {
        Collection<T> allSteps = new ArrayList<>(Arrays.asList(steps));
        allSteps.add((T) wait(Duration.ofSeconds(2)));
        allSteps.add((T) Application.waitUntilReady());
        allSteps.add((T) Application.checkAny());
        allSteps.add((T) User.checkAuthenticated());
        return Task.where(title, allSteps.toArray(new Performable[0]));
    }

    /**
     * Creates an interaction which validates whether the current page is the login page.
     *
     * @return a non-null instance
     */
    public static Performable checkLogin() {
        return Ensure.that("login page", isLogin()).isTrue();
    }

    /**
     * Creates a silent interaction which performs no action (NOOP).
     * <p>
     * The interaction will not be tracked by Serenity
     *
     * @return a non-null instance
     */
    public static Interaction silent() {
        return silent(Application::noop);
    }

    /**
     * Creates a silent interaction, usually related to internal test logic.
     * <p>
     * The interaction will not be tracked by Serenity.
     *
     * @param task the task to execute.
     * @return a non-null instance
     */
    public static Interaction silent(Runnable task) {
        return new SilentInteraction(task);
    }

    /**
     * Creates a fixed delay in the flow.
     *
     * @param duration the duration of the delay.
     * @return a non-null instance
     */
    public static Performable wait(Duration duration) {
        return new WaitPerformable(duration);
    }

    /**
     * Creates an interaction which waits until the default timeout or a condition is satisfied.
     *
     * @param condition the condition
     * @return a non-null instance
     */
    public static Performable waitUntil(Callable<Boolean> condition) {
        return net.serenitybdd.screenplay.waits.Wait.until(condition).forNoMoreThan(timeout);
    }

    /**
     * Creates an interaction which waits until the timeout or a condition is satisfied.
     *
     * @param duration  the maximum wait time
     * @param condition the condition
     * @return a non-null instance
     */
    public static Performable waitUntil(Duration duration, Callable<Boolean> condition) {
        return net.serenitybdd.screenplay.waits.Wait.until(condition).forNoMoreThan(duration);
    }

    /**
     * Creates an interaction which waits until the timeout or a question is satisfied.
     *
     * @param question the question
     * @return a non-null instance
     */
    public static Performable waitUntil(Question<Boolean> question) {
        return net.serenitybdd.screenplay.waits.Wait.until(question, IsAnything.anything()).forNoMoreThan(timeout);
    }

    /**
     * Returns a question which answers whether the application displays the authentication page.
     *
     * @return a non-null instance
     */
    public static Question<Boolean> isLogin() {
        return Question.about("is login page displayed")
                .answeredBy(actor ->Presence.of(By.id("login")).answeredBy(actor));
    }

    /**
     * Creates an interaction which validates whether the current page.
     *
     * @return a non-null instance
     */
    public static Performable checkAny() {
        return Ensure.that("an application page", isAny()).isTrue();
    }

    /**
     * Returns a question which answers whether the application displays the error page.
     *
     * @return a non-null instance
     */
    public static Question<Boolean> isError() {
        return Question.about("is error page")
                .answeredBy(actor -> Presence.of(By.id("error")).answeredBy(actor));
    }

    /**
     * Returns a question which answers whether the application displays any page other then authentication or error.
     *
     * @return a non-null instance
     */
    public static Question<Boolean> isAny() {
        return Question.about("is a application page  displayed")
                .answeredBy(actor -> !isError().answeredBy(actor) || !isLogin()
                        .answeredBy(actor));
    }

    private static void noop() {
        // empty on purpose
    }

    private static class WaitPerformable implements Performable {

        private final Duration duration;

        WaitPerformable(Duration duration) {
            requireNonNull(duration);
            this.duration = duration;
        }

        @Step("{0} waits #target")
        @Override
        public <T extends Actor> void performAs(T actor) {
            ThreadUtils.sleep(duration);
        }
    }

    private static class SilentInteraction extends net.serenitybdd.screenplay.SilentInteraction {

        private final Runnable runnable;

        SilentInteraction(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public <T extends Actor> void performAs(T actor) {
            runnable.run();
        }
    }
}
