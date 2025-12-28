package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception thrown when the server returns a 409 Conflict response.
 */
public class ConflictException extends ApiException {

    public ConflictException(int status, ApiError error) {
        super(status, error);
    }
}
