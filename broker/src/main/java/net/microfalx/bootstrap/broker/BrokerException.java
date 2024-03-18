package net.microfalx.bootstrap.broker;

/**
 * Base class for all broker exceptions.
 */
public class BrokerException extends RuntimeException {

    public BrokerException(String message) {
        super(message);
    }

    public BrokerException(String message, Throwable cause) {
        super(message, cause);
    }
}
