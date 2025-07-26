package net.microfalx.bootstrap.web.dataset;

import jakarta.annotation.security.RolesAllowed;
import net.microfalx.bootstrap.web.application.annotation.SystemTheme;

/**
 * Base class for all data set used for system administration.
 * <p>
 * The system administration uses a different theme (can be the same as the rest of the application) and usually
 * requires the user to have the <code>admin</code> role.
 *
 * @param <M>  the model type
 * @param <ID> the model identifier type
 */
@SystemTheme
@RolesAllowed("admin")
public abstract class SystemDataSetController<M, ID> extends DataSetController<M, ID> {
}
