package net.microfalx.bootstrap.support.report;

/**
 * Base class for all reporting related exceptions.
 */
public class ReportException extends RuntimeException {

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
