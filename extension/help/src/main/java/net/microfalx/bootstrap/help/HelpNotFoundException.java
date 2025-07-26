package net.microfalx.bootstrap.help;

public class HelpNotFoundException extends HelpException {

    public HelpNotFoundException(String message) {
        super(message);
    }

    public HelpNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
