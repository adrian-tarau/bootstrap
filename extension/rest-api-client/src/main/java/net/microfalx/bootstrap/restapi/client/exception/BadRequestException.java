package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception thrown when the server returns a 400 Bad Request response.
 */
public class BadRequestException extends ApiException {

    public BadRequestException(int status, ApiError error) {
        super(status, error);
    }
}
