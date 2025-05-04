package net.microfalx.bootstrap.serenity.task;

import lombok.Getter;
import lombok.ToString;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;

import java.util.ArrayList;
import java.util.Collection;

import static net.microfalx.bootstrap.serenity.task.Application.waitUntilReady;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

@ToString
public class Dashboard<D extends Dashboard<D>> implements Interaction {

    /**
     * The path to the dashboard
     */
    @Getter
    private final String path;

    /**
     * The title of the dashboard
     */
    @Getter
    private final String title;

    /**
     * Creates a dashboard reference.
     *
     * @param path  the path to the dashboard
     * @param title the title of the dashboard
     * @return a non-null instance
     */
    public static Dashboard<?> create(String path, String title) {
        return new Dashboard<>(path, title);
    }

    protected Dashboard(String path, String title) {
        requireNonNull(path);
        requireNotEmpty(title);
        this.path = path;
        this.title = title;
    }

    /**
     * Creates an interaction to open the data set dashboard.
     *
     * @return a non-null instance
     */
    public Task open() {
        return Application.task(
                "{0} opens dashboard '" + title + "'",
                Application.open(path),
                waitUntilReady()
        );
    }

    /**
     * Validates that the grid was loaded and if it has data, reads the records.
     *
     * @return a non-null instance
     */
    public final Task validate() {
        Collection<Performable> performables = new ArrayList<>();

        //card-title
        updateValidation(performables);
        return Task.where(
                "{0} validates dashboard",
                performables.toArray(new Performable[0])
        );
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(open());
    }

    /**
     * Subclasses would add additional steps to validate the dashboard.
     *
     * @param steps a collection of steps
     */
    protected void updateValidation(Collection<Performable> steps) {
        // empty on purpose
    }
}
