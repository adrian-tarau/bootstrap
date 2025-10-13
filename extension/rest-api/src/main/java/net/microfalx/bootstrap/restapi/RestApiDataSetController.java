package net.microfalx.bootstrap.restapi;

import net.microfalx.bootstrap.dataset.annotation.DataSet;

/**
 * Base class for all REST API controllers which manage a dataset (a resource with CRUD).
 * <p>
 * Subclasses must annotate the controller with {@link DataSet} to indicate which dataset they manage.
 *
 * @param <M>  the model type
 * @param <ID> the record identifier type
 */
public abstract class RestApiDataSetController<M, ID> extends RestApiController {

    protected final M get(ID id) {
        return null;
    }

}
