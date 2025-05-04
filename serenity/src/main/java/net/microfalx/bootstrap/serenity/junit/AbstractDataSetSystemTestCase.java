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
        Form form = Form.create();
        form.fieldByLabel("Name", "Serenity Test")
                .fieldByLabel("Enabled", true)
                .fieldByLabel("Roles", "Admin")
                .fieldByLabel("Description", "A security group registered by Serenity");
        actor.attemptsTo(getDataSet().add(form));
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
    protected abstract Form createAddForm();

    /**
     * Creates the form used to edit an existing  record.
     *
     * @return a non-bull instance
     */
    protected abstract Form createEditForm();


}
