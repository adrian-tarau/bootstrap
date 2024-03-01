package net.microfalx.bootstrap.jdbc.support;

import net.microfalx.lang.Identifiable;

/**
 * An interfaces which provides an abstraction over a database transaction.
 */
public interface Transaction extends Identifiable<String> {

    /**
     * Kills the session owning the transaction.
     */
    void kill();
}
