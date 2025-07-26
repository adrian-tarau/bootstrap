package net.microfalx.bootstrap.web.dataset;

import net.microfalx.bootstrap.dataset.DataSetRequest;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Toolbar;

/**
 * An interface for listeners that can be notified of changes in the data set controller.
 */
public interface DataSetControllerListener<M, F extends Field<M>, ID> {

    /**
     * Updates the toolbar based on the current state of the data set and filter.
     *
     * @param request the data set request to update the toolbar for
     * @param toolbar the toolbar to update
     */
    default void updateToolbar(DataSetRequest<M, F, ID> request, Toolbar toolbar) {
        // default implementation does nothing
    }

    /**
     * Updates the action dropdown based on the current state of the data set and filter.
     *
     * @param request the data set request to update the toolbar for
     * @param menu    the toolbar to update
     */
    default void updateActions(DataSetRequest<M, F, ID> request, Menu menu) {
        // default implementation does nothing
    }
}
