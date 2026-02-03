package net.microfalx.bootstrap.feature;

/**
 * An exception thrown when a requested feature is not found.
 */
public class FeatureNotFoundException extends FeatureException {

    public FeatureNotFoundException(String message) {
        super(message);
    }
}
