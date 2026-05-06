package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception thrown when the server accepts the request but it cannot process it and sends a 422 Unprocessable Entity.
 */
public class UnprocessableRequestException extends BadRequestException {

    public UnprocessableRequestException(int status, ApiError error) {
        super(status, error);
    }
}
