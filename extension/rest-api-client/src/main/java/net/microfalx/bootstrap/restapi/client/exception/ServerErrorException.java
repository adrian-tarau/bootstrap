package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception representing server-side errors (HTTP 500).
 */
public class ServerErrorException extends ServerException {

    public ServerErrorException(int status, ApiError error) {
        super(status, error);
    }

    public ServerErrorException(int status, ApiError error, Throwable cause) {
        super(status, error);
        initCause(cause);
    }
}
