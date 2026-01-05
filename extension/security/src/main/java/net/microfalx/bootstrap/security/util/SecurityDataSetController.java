package net.microfalx.bootstrap.security.util;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;

/**
 * Base class for all security controller
 *
 * @param <M>  the model type
 * @param <ID> the model identifier type
 */
public abstract class SecurityDataSetController<M, ID> extends SystemDataSetController<M, ID> {

    public SecurityDataSetController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
