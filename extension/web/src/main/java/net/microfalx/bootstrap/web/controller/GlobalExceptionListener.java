package net.microfalx.bootstrap.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.bootstrap.core.utils.Failure;
import net.microfalx.bootstrap.support.report.Issue;
import net.microfalx.bootstrap.web.util.PathFilter;
import net.microfalx.lang.UriUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionListener implements HandlerExceptionResolver {

    private static final Map<Failure.Type, Issue.Severity> FAILURE_SEVERITIES = new HashMap<>();

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String matchedPattern = PathFilter.getRequestPattern(request);
        Issue.Severity severity = getSeverityFromException(request, ex);
        if (severity != null) {
            Issue.create(Issue.Type.STABILITY, matchedPattern).withDescription(ex, "Unhandled exception in controller")
                    .withModule("Controller").withSeverity(severity).withAttributeCounter(PathFilter.getRootPath(request))
                    .register();
        }
        return null;
    }

    private Issue.Severity getSeverityFromException(HttpServletRequest request, Exception ex) {
        String rootPath = PathFilter.getRootPath(request);
        Failure.Type type = Failure.getType(ex);
        if (type.isSecurity() && UriUtils.isRoot(rootPath)) return null;
        Issue.Severity severity = FAILURE_SEVERITIES.get(type);
        if (severity == null) severity = Issue.Severity.HIGH;
        return severity;
    }

    static {
        FAILURE_SEVERITIES.put(Failure.Type.AUTHORIZATION, Issue.Severity.CRITICAL);
        FAILURE_SEVERITIES.put(Failure.Type.AUTHENTICATION, Issue.Severity.HIGH);
        FAILURE_SEVERITIES.put(Failure.Type.ILLEGAL_INPUT, Issue.Severity.LOW);
        FAILURE_SEVERITIES.put(Failure.Type.ILLEGAL_OUTPUT, Issue.Severity.LOW);
        FAILURE_SEVERITIES.put(Failure.Type.CONNECTIVITY, Issue.Severity.LOW);
        FAILURE_SEVERITIES.put(Failure.Type.NETWORK, Issue.Severity.LOW);
        FAILURE_SEVERITIES.put(Failure.Type.TIMED_OUT, Issue.Severity.LOW);
        FAILURE_SEVERITIES.put(Failure.Type.RESOURCE_NOT_FOUND, Issue.Severity.LOW);
        FAILURE_SEVERITIES.put(Failure.Type.SERVICE_UNAVAILABLE, Issue.Severity.HIGH);
        FAILURE_SEVERITIES.put(Failure.Type.CONFIGURATION, Issue.Severity.CRITICAL);
    }
}
