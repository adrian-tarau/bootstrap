package net.microfalx.bootstrap.restapi;

import net.microfalx.metrics.Metrics;

/**
 * Various utilities for REST API.
 */
public class RestApiUtils {

    static final Metrics METRICS = Metrics.of("REST API");
    static final Metrics VALIDATION_METRICS = METRICS.withGroup("Validation");
    static final Metrics VALIDATION_STATUS_METRICS = VALIDATION_METRICS.withGroup("Status");
}
