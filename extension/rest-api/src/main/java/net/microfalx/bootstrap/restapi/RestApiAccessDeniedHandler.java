package net.microfalx.bootstrap.restapi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class RestApiAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = new RestApiError(RestApiError.FORBIDDEN, req.getRequestURI()).setMessage("Forbidden")
                .setDescription("You do not have permission to access this resource");
        RestApiJson.DEFAULT.write(res.getOutputStream(), body);
    }
}
