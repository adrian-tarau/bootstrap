package net.microfalx.bootstrap.feature;

/**
 * Base class for all feature-related exceptions.
 */
public class FeatureException extends RuntimeException {

    public FeatureException(String message) {
        super(message);
    }

    public FeatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
