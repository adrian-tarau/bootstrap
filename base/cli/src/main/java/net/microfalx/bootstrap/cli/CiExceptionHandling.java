package net.microfalx.bootstrap.cli;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import picocli.CommandLine;

/**
 * A generic exception handle.
 */
@Slf4j
public class CiExceptionHandling implements CommandLine.IExecutionExceptionHandler {

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult fullParseResult) throws Exception {
        LOGGER.atError().setCause(ex).log("Failed to executed command, arguments {}",  fullParseResult.originalArgs());
        String message = "@|red ERROR: " + ExceptionUtils.getRootCauseMessage(ex) + "|@";
        commandLine.getErr().println(CommandLine.Help.Ansi.AUTO.string(message));
        return 1;
    }
}
