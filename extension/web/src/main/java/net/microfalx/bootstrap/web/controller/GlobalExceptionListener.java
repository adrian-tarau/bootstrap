package net.microfalx.bootstrap.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.bootstrap.support.report.Issue;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionListener implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Issue.create(Issue.Type.STABILITY, "Controller")
                .withDescription(ex, "Unhandled exception in controller")
                .withModule("Controller").withSeverity(Issue.Severity.HIGH).register();
        return null;
    }
}
