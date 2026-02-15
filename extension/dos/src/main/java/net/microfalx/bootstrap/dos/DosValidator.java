package net.microfalx.bootstrap.dos;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.bootstrap.web.util.HttpServletUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static net.microfalx.bootstrap.dos.DosUtils.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.IOUtils.closeQuietly;

/**
 * A utility class which validates incoming DOS requests.
 */
public class DosValidator {

    private static final String DOS_BLOCK_HEADER = "X-DoS-Block";

    private final DosService dosService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final Request dosRequest;

    public DosValidator(DosService dosService, HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireNonNull(dosService);
        requireNonNull(request);
        requireNonNull(response);
        this.dosService = dosService;
        this.request = request;
        this.response = response;
        this.dosRequest = extractRoutingRequest();
    }

    /**
     * Handles the request by checking whether it should be rejected.
     * <p>
     * The method would be called from a filter before the request is processed.
     *
     * @return <code>true</code> if the request should proceed, <code>false</code> if it was rejected
     * @throws IOException if an I/O error occurs
     */
    boolean preHandle() throws IOException {
        if (shouldReject(request)) {
            response.setHeader(DOS_BLOCK_HEADER, "true");
            DosValidator.sendResponse(response, SC_FORBIDDEN, DOS_ACCESS_DENIED);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Called after the request has been processed to register the outcome.
     *
     * @param response the response object
     */
    void afterCompletion(HttpServletResponse response, Exception exception) {
        if (dosRequest == null) return;
        try {
            int status = response.getStatus();
            Request finalDosRequest = dosRequest.withOutcome(getOutcomeFromHttp(status, exception));
            dosService.register(finalDosRequest);
        } catch (Exception e) {
            ERROR.increment(ExceptionUtils.getRootCauseName(e));
        }
    }

    private boolean shouldReject(HttpServletRequest request) {
        if (!dosService.isEnabled() || dosRequest == null) return false;
        try {
            Rule.Action action = dosService.validate(dosRequest);
            return action == Rule.Action.DENY;
        } catch (Exception e) {
            ERROR.increment(ExceptionUtils.getRootCauseName(e));
            return false;
        }
    }

    private Request extractRoutingRequest() throws IOException {
        try {
            String address = HttpServletUtils.getClientIp(request);
            if (StringUtils.isEmpty(address)) address = "localhost";
            return Request.create(getAbsoluteUri(request), address, Request.Outcome.NONE);
        } catch (Exception e) {
            if (ExceptionUtils.contains(e, IllegalArgumentException.class) && ExceptionUtils.contains(e, URISyntaxException.class)) {
                String message = "Invalid URI '" + getSafeAbsoluteUri(request) + "'";
                sendResponse(response, message, e);
            } else {
                String message = "Invalid request: " + getSafeAbsoluteUri(request);
                sendResponse(response, message, e);
            }
            return null;
        }
    }


    private String getSafeAbsoluteUri(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    private void sendResponse(HttpServletResponse response, String message, Exception e) throws IOException {
        DosUtils.ERROR.increment(ExceptionUtils.getRootCauseName(e));
        sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
    }

    /**
     * Sends a text response with a status code.
     *
     * @param response the HTTP response
     * @param status   the status
     * @param message  the message
     * @throws IOException if an I/O error occurs
     */
    static void sendResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        PrintWriter writer = response.getWriter();
        writer.write(message);
        closeQuietly(writer);
    }

    private static final String DOS_ACCESS_DENIED = "Access denied due to DoS rules. If you think this is an error on our part, please contact system administrator";
}
