package net.microfalx.bootstrap.web.container;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZoneOffset;

import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Various utilities for web container.
 */
public class WebContainerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebContainerMvcConfig.class);

    /**
     * A custom HTTP header with a time zone used by data sets to display temporals in the zone of the user.
     */
    public static final String TIMEZONE_HEADER = "X-TimeZone";

    /**
     * An HTTP session attribute which stores the client time zone in the HTTP session
     */
    public static final String TIMEZONE_SESSION_ATTR = "TimeZone";

    /**
     * Normalizes the zone offset, if needed from minutes to hours + minutes.
     *
     * @param timeZone the time zone offset.
     * @return the normalize time zone offset
     */
    public static String normalizeZoneOffset(String timeZone) {
        if (timeZone.contains(":")) {
            return timeZone;
        } else {
            int minutes = Math.abs(Integer.parseInt(timeZone));
            int hours = (minutes / 60);
            minutes = (minutes % 60);
            String sign = timeZone.startsWith("-") ? "-" : "+";
            return sign + StringUtils.leftPad(Integer.toString(hours), 2, '0') + ":" +
                    StringUtils.leftPad(Integer.toString(minutes), 2, '0');
        }
    }

    /**
     * Returns client timezone, if available
     * <p>
     * This function assumes that the {@link #TIMEZONE_HEADER} request header is set and
     * in the form <code>[+-]hh:mm</code> or <code>[+/-]mm</code>
     *
     * @return client time zone or null if not known
     */
    public static ZoneId getTimeZone(HttpServletRequest request) {
        String timezone = request.getHeader(TIMEZONE_HEADER);
        HttpSession session = request.getSession();
        if (isEmpty(timezone) && session != null) {
            timezone = (String) session.getAttribute(TIMEZONE_SESSION_ATTR);
        }
        if (isNotEmpty(timezone)) {
            if (session != null) session.setAttribute(TIMEZONE_SESSION_ATTR, timezone);
            try {
                return ZoneOffset.of(normalizeZoneOffset(timezone));
            } catch (Exception e) {
                LOGGER.warn("Failed to parse timezone (" + timezone + ") from the HTTP request, reason:  " + e.getMessage());
            }
        }
        return null;
    }
}
