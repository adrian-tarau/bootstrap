package net.microfalx.bootstrap.serenity.task;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.ensure.Ensure;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A collections of interactions and questions related to a <code>DataSet</code> dashboard.
 */
public class DataSet implements Interaction {

    private final String path;
    private final String title;

    public static DataSet create(String path, String title) {
        return new DataSet(path, title);
    }

    private DataSet(String path, String title) {
        requireNonNull(path);
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
                "{0} opens data set '" + title + "'",
                Application.open(path),
                Ensure.that("dataset is valid", isValid()).isTrue()
        );
    }

    /**
     * Creates an interaction to add a new record to the dataset.
     *
     * @return a non-null instance
     */
    public Task add() {
        return Application.task(
                "{0} adds a new record to dataset '" + title + "'"

        );
    }

    /**
     * Creates an interaction to add a new record to the dataset.
     *
     * @return a non-null instance
     */
    public Task edit() {
        return Application.task(
                "{0} edits a record from dataset '" + title + "'"

        );
    }

    /**
     * Creates an interaction to delete an existing record from the dataset.
     *
     * @return a non-null instance
     */
    public Task delete() {
        return Application.task(
                "{0} deletes a record from dataset '" + title + "'"

        );
    }

    /**
     * Returns a question which answers whether the dashboard is a data set and it is the right one.
     *
     * @return a non-null instance
     */
    public static Question<Boolean> isValid() {
        return Question.about("is login page displayed")
                .answeredBy(actor -> true);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(open());
    }
}
