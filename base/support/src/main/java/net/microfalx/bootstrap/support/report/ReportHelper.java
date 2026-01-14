package net.microfalx.bootstrap.support.report;

import net.microfalx.lang.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * Various helper methods for reports.
 */
public class ReportHelper {

    private static final ZonedDateTime startupTime = ZonedDateTime.now();
    private final ZonedDateTime currentTime = ZonedDateTime.now();

    public ZonedDateTime getStartupTime() {
        return startupTime;
    }

    public ZonedDateTime getCurrentTime() {
        return currentTime;
    }

    public String formatDateTime(Object temporal) {
        return FormatterUtils.formatDateTime(temporal);
    }

    public String formatBytes(Number value) {
        return FormatterUtils.formatBytes(value);
    }

    public String formatPercent(Number value) {
        return FormatterUtils.formatPercent(value);
    }

    public String formatDuration(Duration duration) {
        return FormatterUtils.formatDuration(duration);
    }

    public String formatNumber(Number number) {
        return FormatterUtils.formatNumber(number);
    }

    public String toString(Object value) {
        if (value instanceof Collection<?>) {
            StringBuilder builder = new StringBuilder();
            for (Object o : (Collection<?>) value) {
                StringUtils.append(builder, o, ", ");
            }
            return builder.toString();
        } else {
            return ObjectUtils.toString(value);
        }
    }

    public String toDisplay(Object value) {
        String text = toString(value);
        return StringUtils.isEmpty(text) ? "-" : text;
    }

    @SuppressWarnings("unchecked")
    public String toLabel(Object value) {
        if (value instanceof Enum) {
            return EnumUtils.toLabel((Enum) value);
        } else if (value instanceof Class<?> clazz) {
            return ClassUtils.getCompactName(clazz);
        } else {
            return toDisplay(value);
        }
    }

    public String toHtmlId(Object value) {
        if (value == null) return null;
        return "#" + ObjectUtils.toString(value);
    }

    static final long[] DURATION_BUCKETS = new long[]{
            1, 5, 10, 20, 50, 100, 200, 500, 1_000, 2_000, 5_000, 10_000, 20_000, 30_000, 60_000
    };
    private static final int DURATION_BUCKETS_LENGTH = DURATION_BUCKETS.length;

    static final String[] DURATION_BUCKET_NAMES = new String[]{
            "<1ms", "5ms", "10ms", "20ms", "50ms", "100ms", "200ms", "500ms", "1s", "2s", "5s", "10s", "20s", "30s", ">60s"
    };

}
