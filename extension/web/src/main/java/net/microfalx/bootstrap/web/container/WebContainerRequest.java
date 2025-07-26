package net.microfalx.bootstrap.web.container;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.time.ZoneId;
import java.util.Locale;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which provides access to the current web container request for the current thread.
 * <p>
 * The class expects that calls to this class are executed under an HTTP(s) request.
 */
public class WebContainerRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebContainerRequest.class);

    static ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<>();

    private final HttpServletRequest request;

    /**
     * Returns the request context associated with the current thread.
     *
     * @return a non-null instance
     */
    public static WebContainerRequest get() {
        return new WebContainerRequest(REQUEST.get());
    }

    /**
     * Returns the request context for a given request.
     *
     * @return a non-null instance
     */
    public static WebContainerRequest create(HttpServletRequest request) {
        return new WebContainerRequest(request);
    }

    WebContainerRequest(HttpServletRequest request) {
        requireNonNull(request);
        this.request = request;
    }

    /**
     * Returns the HTTP servlet request.
     *
     * @return a non-null instance
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Returns the HTTP servlet session.
     *
     * @return a non-null instance
     */
    public HttpSession getSession() {
        return request.getSession();
    }

    /**
     * Returns a request parameter.
     *
     * @param name the name of the parameter
     * @return the value
     */
    public String getParameter(String name) {
        requireNonNull(name);
        return request.getParameter(name);
    }

    /**
     * Returns a request parameter as an integer.
     *
     * @param name         the name of the parameter
     * @param defaultValue the default value
     * @return the value as int
     */
    public int getParameter(String name, int defaultValue) {
        String value = getParameter(name);
        if (StringUtils.isEmpty(value)) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns a request header.
     *
     * @param name the name of the header
     * @return the value
     */
    public String getHeader(String name) {
        requireNonNull(name);
        return request.getHeader(name);
    }

    /**
     * Returns a session attribute.
     *
     * @param name the name of the session attribute
     * @param <T>  the type of the result
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) getSession().getAttribute(name);
    }

    /**
     * Returns a session attribute.
     *
     * @param name         the name of the session attribute
     * @param defaultValue the default value
     * @param <T>          the type of the result
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, T defaultValue) {
        T value = (T) getSession().getAttribute(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Returns the time zone from the client
     *
     * @return the time zone of the client or server zone if not available
     * @see WebContainerUtils#getTimeZone(HttpServletRequest)
     */
    public ZoneId getTimeZone() {
        ZoneId timeZone = WebContainerUtils.getTimeZone(request);
        if (timeZone == null) timeZone = LocaleContextHolder.getTimeZone().toZoneId();
        return timeZone != null ? timeZone : ZoneId.systemDefault();
    }

    /**
     * Returns whether the request has client time zone information.
     *
     * @return {@code true} if time zone information is available, {@code false} otherwise
     */
    public boolean hasTimeZone() {
        return WebContainerUtils.getTimeZone(request) != null;
    }

    /**
     * Returns the locale associated with the request.
     *
     * @return a non-null instance
     */
    @SuppressWarnings("ReassignedVariable")
    public Locale getLocale() {
        Locale locale = RequestContextUtils.getLocale(request);
        if (locale == null) locale = LocaleContextHolder.getLocale();
        return locale != null ? locale : Locale.getDefault();
    }

    @Override
    public String toString() {
        return "WebContainerRequest{" +
                "request=" + request.getRequestURI() +
                '}';
    }
}
