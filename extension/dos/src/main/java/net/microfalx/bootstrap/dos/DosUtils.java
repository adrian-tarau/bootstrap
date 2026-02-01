package net.microfalx.bootstrap.dos;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.TimeUtils;
import net.microfalx.lang.UriUtils;
import net.microfalx.metrics.Metrics;

import java.net.URI;
import java.time.Duration;

import static jakarta.servlet.http.HttpServletResponse.*;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.isEmpty;

/**
 * Various utilities for DoS Service.
 */
@Slf4j
public class DosUtils {

    private static final String HTTP_HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    static final Metrics METRICS = Metrics.of("Denial of Service");
    static final Metrics ERROR = METRICS.withGroup("Error");
    static final Metrics REQUEST = METRICS.withGroup("Request");
    static final Metrics REQUEST_HOSTNAME = REQUEST.withGroup("Hostname");
    static final Metrics REQUEST_PROTOCOL = REQUEST.withGroup("Protocol");
    static final Metrics REQUEST_OUTCOME = REQUEST.withGroup("Outcome");

    static final Metrics CROSS = METRICS.withGroup("Cross");
    static final Metrics ALERT = METRICS.withGroup("Alert");
    static final Metrics RESET = METRICS.withGroup("Reset");
    static final Metrics THROTTLE = METRICS.withGroup("Throttle");

    static final Metrics RESOLVE = METRICS.withGroup("Resolve");

    /**
     * A minimum count required to create a throughput
     */
    static final int MINIMUM_COUNT = 20;

    /**
     * A generous limit to
     */
    protected static final long DNS_CACHE_REFRESH = TimeUtils.FIFTEEN_MINUTE;

    /**
     * Parses a request rate from its string representation.
     *
     * @param requestRate the request rate as a string
     * @return the request rate in requests per second
     */
    public static float parseRequestRate(String requestRate) {
        if (StringUtils.isEmpty(requestRate)) return 1;
        requestRate = requestRate.toLowerCase().trim();
        if (requestRate.endsWith("r/s")) {
            String rate = requestRate.replace("r/s", EMPTY_STRING);
            return Float.parseFloat(rate);
        } else if (requestRate.endsWith("r/m")) {
            String rate = requestRate.replace("r/m", EMPTY_STRING);
            return Float.parseFloat(rate) / 60f;
        } else if (requestRate.endsWith("r/h")) {
            String rate = requestRate.replace("r/h", EMPTY_STRING);
            return Float.parseFloat(rate) / 3600f;
        } else {
            throw new IllegalArgumentException("Invalid request rate value: " + requestRate);
        }
    }

    /**
     * Parses a threshold from its string representation.
     *
     * @param value the value to parse
     * @return a non-null instance
     */
    public static Threshold parseThreshold(String value) {
        String[] parts = StringUtils.split(value, ",");
        if (parts.length != 2) {
            LOGGER.error("Invalid threshold value: {}, fallback to default", value);
            return Threshold.DEFAULT_THRESHOLD;
        } else {
            float requestRate = DosUtils.parseRequestRate(parts[0]);
            Duration period = TimeUtils.parseDuration(parts[1]);
            return Threshold.create(requestRate, period);
        }
    }

    /**
     * Returns the DoS outcome for a request.
     *
     * @param statusCode the HTTP status code
     * @return a non-null instance
     */
    public static Request.Outcome getOutcomeFromHttp(Integer statusCode) {
        if (statusCode == null) return Request.Outcome.SUCCESS;
        if (statusCode < SC_BAD_REQUEST) {
            return Request.Outcome.SUCCESS;
        } else if (statusCode >= SC_INTERNAL_SERVER_ERROR) {
            return Request.Outcome.FAILURE;
        } else {
            Request.Outcome outcome = Request.Outcome.FAILURE;
            outcome = switch (statusCode) {
                case SC_METHOD_NOT_ALLOWED, SC_NOT_ACCEPTABLE, SC_UNSUPPORTED_MEDIA_TYPE -> Request.Outcome.INVALID;
                case SC_BAD_REQUEST -> Request.Outcome.VALIDATION;
                case SC_NOT_FOUND -> Request.Outcome.NOT_FOUND;
                case SC_UNAUTHORIZED -> Request.Outcome.AUTHENTICATION;
                case SC_FORBIDDEN -> Request.Outcome.AUTHORIZATION;
                default -> outcome;
            };
            return outcome;
        }
    }

    /**
     * Returns client identification, as reported by a proxy or as it comes in the request
     *
     * @param request a servlet request
     * @return a string identifying the client (usually an IP)
     */
    public static String getFirstClientInfo(HttpServletRequest request) {
        String forwardedHost = request.getHeader(HTTP_HEADER_X_FORWARDED_FOR);
        if (isEmpty(forwardedHost)) {
            forwardedHost = request.getRemoteAddr();
        } else {
            forwardedHost = StringUtils.split(forwardedHost, ",")[0];
        }
        return forwardedHost;
    }

    /**
     * Returns the absolute URI for a HTTP request.
     *
     * @param request the request
     * @return the URI, null if it cannot be determined
     */
    public static URI getAbsoluteUri(HttpServletRequest request) {
        try {
            return UriUtils.parseUri(request.getRequestURL().toString());
        } catch (Exception e) {
            return null;
        }
    }

}
