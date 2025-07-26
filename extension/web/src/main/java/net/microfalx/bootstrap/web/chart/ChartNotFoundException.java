package net.microfalx.bootstrap.web.chart;

/**
 * An exception raised when a chart or a chart related element cannot be located.
 */
public class ChartNotFoundException extends ChartException {

    public ChartNotFoundException(String message) {
        super(message);
    }
}
