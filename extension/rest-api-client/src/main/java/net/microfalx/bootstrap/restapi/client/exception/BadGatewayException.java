package net.microfalx.bootstrap.restapi.client.exception;

/**
 * An exception representing a gateway/proxy (HTTP 502).
 */
public class BadGatewayException extends ServerException {

    public BadGatewayException(int status, ApiError error) {
        super(status, error);
    }
}
