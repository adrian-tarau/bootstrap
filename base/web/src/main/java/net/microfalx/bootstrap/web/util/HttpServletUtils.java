package net.microfalx.bootstrap.web.util;

import jakarta.servlet.http.HttpServletRequest;
import net.microfalx.lang.StringUtils;

import static net.microfalx.lang.StringUtils.isEmpty;

/**
 * Various utilities related to HttpServletRequest and HttpServletResponse.
 */
public class HttpServletUtils {

    private static final String HTTP_HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HTTP_HEADER_X_HEALTH_CHECK = "X-Health-Check";

    /**
     * Returns the client IP address from the request, taking into account the
     * "X-Forwarded-For" header if present.
     *
     * @param request the HTTP servlet request
     * @return a non-null string containing the client IP address
     */
    public static String getClientIp(HttpServletRequest request) {
        String forwardedHost = request.getHeader(HTTP_HEADER_X_FORWARDED_FOR);
        if (isEmpty(forwardedHost)) {
            forwardedHost = request.getRemoteAddr();
        } else {
            forwardedHost = StringUtils.split(forwardedHost, ",")[0];
        }
        return forwardedHost;
    }
}
