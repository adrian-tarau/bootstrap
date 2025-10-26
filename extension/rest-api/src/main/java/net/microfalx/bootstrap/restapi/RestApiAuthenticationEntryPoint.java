package net.microfalx.bootstrap.restapi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class RestApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = new RestApiError(RestApiError.UNAUTHORIZED, req.getRequestURI()).setMessage("Unauthorized")
                .setDescription("Authentication is required to access this resource");
        RestApiJson.DEFAULT.write(res.getOutputStream(), body);
    }
}
