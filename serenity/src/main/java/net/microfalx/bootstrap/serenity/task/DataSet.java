package net.microfalx.bootstrap.serenity.task;

import lombok.Getter;
import lombok.ToString;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Evaluate;
import net.serenitybdd.screenplay.ensure.Ensure;

/**
 * A collections of interactions and questions related to a <code>DataSet</code> dashboard.
 */
@ToString(callSuper = true)
public class DataSet extends Dashboard<DataSet> implements Interaction {

    /**
     * Holds whether the data set has CRUD operations available.
     */
    @Getter
    private boolean crud = true;

    /**
     * Creates a data set dashboard reference.
     *
     * @param path  the path to the dashboard
     * @param title the title of the dashboard
     * @return a non-null instance
     */
    public static DataSet create(String path, String title) {
        return new DataSet(path, title);
    }

    private DataSet(String path, String title) {
        super(path, title);
    }

    /**
     * Changes whether the data set has CRUD operations available.
     *
     * @param crud {@code true} if CRUD is available, {@code false} otherwise
     * @return self
     */
    public DataSet setCrud(boolean crud) {
        this.crud = crud;
        return self();
    }

    /**
     * Creates an interaction to add a new record to the dataset.
     *
     * @return a non-null instance
     */
    public Task add(Form form) {
        return Application.task(
                "{0} adds a new record to dataset '" + getTitle() + "'",
                Evaluate.javascript("DataSet.add()"),
                form.fill(),
                form.submit(),
                Ensure.that(Question.not(form.isPresent())).isTrue()
        );
    }

    /**
     * Creates an interaction to add a new record to the dataset.
     *
     * @param id   the identifier of the record being changed
     * @param form the fields changed during edit
     * @return a non-null instance
     */
    public <ID> Task edit(ID id, Form form) {
        return Application.task(
                "{0} edits a record from dataset '" + getTitle() + "'"

        );
    }

    /**
     * Creates an interaction to delete an existing record from the dataset.
     *
     * @param id the identifier of the record being removed
     * @return a non-null instance
     */
    public <ID> Task delete(ID id) {
        return Application.task(
                "{0} deletes a record from dataset '" + getTitle() + "'"

        );
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(open());
    }
}
