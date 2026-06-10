package net.microfalx.bootstrap.cli;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.lang.ExceptionUtils;

/**
 * Base exception for all CLI exceptions.
 */
@Getter
@Setter
public class CliException extends RuntimeException {

    private String log;

    public CliException(String message) {
        super(message);
    }

    public CliException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", getMessage())
                .add("rootCause", ExceptionUtils.getRootCauseDescription(this))
                .add("log", log)
                .toString();
    }
}
