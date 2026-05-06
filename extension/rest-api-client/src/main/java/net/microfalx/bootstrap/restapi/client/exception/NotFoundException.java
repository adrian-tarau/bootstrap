package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception thrown when the server returns a 404 Not Found response.
 */
public class NotFoundException extends ClientException {

    public NotFoundException(int status, ApiError error) {
        super(status, error);
    }
}
