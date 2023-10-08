package net.microfalx.bootstrap.web.dashboard;

/**
 * Base exception for all dashboard exceptions.
 */
public class DashboardException extends RuntimeException {

    public DashboardException(String message) {
        super(message);
    }

    public DashboardException(String message, Throwable cause) {
        super(message, cause);
    }
}
