package net.microfalx.bootstrap.dataset;

/**
 * An exception raised during an export.
 */
public class DataSetExportException extends DataSetException {

    public DataSetExportException(String message) {
        super(message);
    }

    public DataSetExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
