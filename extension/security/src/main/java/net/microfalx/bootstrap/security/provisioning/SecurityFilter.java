package net.microfalx.bootstrap.security.provisioning;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

public class SecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
        Collection<String> headers = response.getHeaders(SET_COOKIE);
        boolean first = true;
        for (String header : headers) {
            String updated = header + "; SameSite=Lax";
            if (first) {
                response.setHeader(SET_COOKIE, updated);
                first = false;
            } else {
                response.addHeader(SET_COOKIE, updated);
            }
        }
    }
}
