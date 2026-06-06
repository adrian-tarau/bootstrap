package net.microfalx.bootstrap.cli;

import org.apache.commons.lang3.exception.ExceptionUtils;
import picocli.CommandLine;

/**
 * A generic exception handle.
 */
public class CiExceptionHandling implements CommandLine.IExecutionExceptionHandler {

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult fullParseResult) throws Exception {
        String message = "@|red ERROR: " + ExceptionUtils.getRootCauseMessage(ex) + "|@";
        commandLine.getErr().println(CommandLine.Help.Ansi.AUTO.string(message));
        return 1;
    }
}
