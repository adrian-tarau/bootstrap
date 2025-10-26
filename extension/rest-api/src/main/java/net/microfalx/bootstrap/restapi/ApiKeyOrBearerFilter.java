package net.microfalx.bootstrap.restapi;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.lang.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A filter which authenticates requests using either API Key or Bearer token.
 */
class ApiKeyOrBearerFilter extends OncePerRequestFilter {

    private final ApiCredentialService credentialService;

    ApiKeyOrBearerFilter(ApiCredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            var authorization = request.getHeader("Authorization");
            var apiKey = request.getHeader("X-API-KEY");
            if (isNotEmpty(authorization) && authorization.startsWith("Bearer ")) {
                var token = StringUtils.trim(authorization.substring(7));
                var user = credentialService.authenticateBearer(token);
                if (user != null) {
                    var auth = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } else if (isNotEmpty(apiKey)) {
                var user = credentialService.authenticateApiKey(StringUtils.trim(apiKey));
                if (user != null) {
                    var auth = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
