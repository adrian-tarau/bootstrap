package net.microfalx.bootstrap.serenity.junit;

import net.microfalx.bootstrap.serenity.task.DataSet;
import net.microfalx.bootstrap.serenity.task.Form;
import org.junit.jupiter.api.*;

/**
 * Base class for all data set dashboards.
 */
@Tag("dataset")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractDataSetSystemTestCase extends AbstractDashboardSystemTestCase<DataSet> {

    @Test
    @Order(10)
    void add() {
        if (!getDataSet().isCrud()) return;
        Form form = createAddForm();
        actor.attemptsTo(getDataSet().add(form));
    }

    @Test
    @Order(11)
    void edit() {
        if (!getDataSet().isCrud()) return;
        Form form = createEditForm();
        actor.attemptsTo(getDataSet().add(form));
    }

    @Test
    @Order(12)
    void delete() {
        if (!getDataSet().isCrud()) return;
        actor.attemptsTo(getDataSet().delete(null));
    }

    @Override
    protected final DataSet createDashboard() {
        return createDataSet();
    }

    /**
     * Returns a reference to a data set dashboard.
     *
     * @return a non-null instance
     */
    protected final DataSet getDataSet() {
        return getDashboard();
    }

    /**
     * Creates the {@link DataSet} used to perform interactions with a dashboard.
     *
     * @return a non-null instance
     */
    protected abstract DataSet createDataSet();

    /**
     * Creates the form used to add a new record.
     *
     * @return a non-bull instance
     */
    protected Form createAddForm() {
        throw new IllegalStateException("Data Set dashboards with CRUD enabled requires a form");
    }

    /**
     * Creates the form used to edit an existing  record.
     *
     * @return a non-bull instance
     */
    protected Form createEditForm() {
        return createAddForm();
    }


}
