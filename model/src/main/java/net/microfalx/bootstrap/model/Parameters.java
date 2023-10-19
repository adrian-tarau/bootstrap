package net.microfalx.bootstrap.model;

/**
 * Manages a collection of {@link Parameter parameters}.
 */
public interface Parameters extends Attributes<Parameter> {

    /**
     * Creates an instance of parameters with default implementation.
     *
     * @return a non-null instance
     */
    static Parameters create() {
        return new DefaultParameters();
    }
}
