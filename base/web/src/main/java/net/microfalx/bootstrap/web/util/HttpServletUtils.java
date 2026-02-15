package net.microfalx.bootstrap.web.util;

import jakarta.servlet.http.HttpServletRequest;
import net.microfalx.bootstrap.core.utils.Failure;
import net.microfalx.lang.StringUtils;
import org.eclipse.jetty.ee10.servlet.QuietServletException;
import org.eclipse.jetty.io.EofException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    static {
        Failure.registerType(Failure.Type.RESOURCE_NOT_FOUND, NoResourceFoundException.class);
        Failure.registerType(Failure.Type.RESOURCE_NOT_FOUND, NoHandlerFoundException.class);
        Failure.registerType(Failure.Type.RESET, QuietServletException.class);
        Failure.registerType(Failure.Type.RESET, EofException.class);
        Failure.registerSubType(Failure.Type.ILLEGAL_INPUT, HttpMediaTypeException.class);
    }
}
