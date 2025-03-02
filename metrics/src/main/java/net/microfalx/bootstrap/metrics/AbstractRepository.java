package net.microfalx.bootstrap.metrics;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.metrics.Repository;

/**
 * Base class for repositories aware of application context.
 * <p>
 * Most repositories are always available, and they need to be able to get a reference to various services.
 */
public abstract class AbstractRepository extends ApplicationContextSupport implements Repository {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getLastError() {
        return null;
    }
}
