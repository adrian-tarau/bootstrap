package net.microfalx.bootstrap.restapi.client.exception;

import lombok.Getter;
import lombok.ToString;

/**
 * Base class for API exceptions.
 */
@Getter
@ToString
public abstract class ApiException extends RuntimeException {

    private final int status;
    private final ApiError error;

    protected ApiException(int status, ApiError error) {
        super(error != null ? error.getMessage() : "API error");
        this.status = status;
        this.error = error;
    }
}
