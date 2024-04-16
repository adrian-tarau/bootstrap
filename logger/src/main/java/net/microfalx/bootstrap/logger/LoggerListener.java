package net.microfalx.bootstrap.logger;

import net.microfalx.lang.annotation.Provider;

/**
 * An interface used to store logger events.
 */
@Provider
public interface LoggerListener {

    /**
     * Stores a log event in the store.
     *
     * @param event the log event
     */
    void onEvent(LoggerEvent event);
}
