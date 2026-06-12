package net.microfalx.bootstrap.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.bootstrap.core.utils.Failure;
import net.microfalx.bootstrap.support.report.Issue;
import net.microfalx.bootstrap.web.util.PathFilter;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.UriUtils;
import net.microfalx.metrics.Metrics;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionListener implements HandlerExceptionResolver {

    private static final Map<Failure.Type, Issue.Severity> FAILURE_SEVERITIES = new HashMap<>();
    private static final Set<String> FAILURE_PATH_IGNORES = new HashSet<>();
    private static final PathFilter DEFAULT_EXCLUSIONS = new PathFilter();

    private static final Metrics FAILURE = Metrics.of("Bootstrap").withGroup("Controller Failures");
    private static final Metrics FAILURE_BY_EXCEPTION = FAILURE.withGroup("Exception");
    private static final Metrics FAILURE_BY_PATH = FAILURE.withGroup("Path");

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        FAILURE_BY_EXCEPTION.count(ClassUtils.getCompactName(exception));
        FAILURE_BY_PATH.count(PathFilter.getRootPath(request));
        if (shouldIgnorePath(request) || shouldIgnoreException(request, exception)) return null;
        String matchedPattern = PathFilter.getRequestPattern(request);
        Issue.Severity severity = getSeverityFromException(request, exception);
        if (severity != null) {
            Issue.create(Issue.Type.STABILITY, matchedPattern).withDescription(exception, "Unhandled exception in controller")
                    .withModule("Controller").withSeverity(severity).withAttributeCounter(PathFilter.getRootPath(request))
                    .register();
        }
        return null;
    }

    private boolean shouldIgnoreException(HttpServletRequest request, Exception exception) {
        Failure.Type type = Failure.getType(exception);
        if (type == Failure.Type.AUTHORIZATION) {
            return DEFAULT_EXCLUSIONS.isRoot(request) || DEFAULT_EXCLUSIONS.shouldExclude(request);
        } else {
            return false;
        }
    }

    private boolean shouldIgnorePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        PathMatcher matcher = new AntPathMatcher();
        for (String pathIgnore : FAILURE_PATH_IGNORES) {
            if (matcher.match(pathIgnore, uri)) return true;
        }
        return false;
    }

    private Issue.Severity getSeverityFromException(HttpServletRequest request, Exception exception) {
        String rootPath = PathFilter.getRootPath(request);
        Failure.Type type = Failure.getType(exception);
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

        FAILURE_PATH_IGNORES.add("/**/*.php");
    }
}
