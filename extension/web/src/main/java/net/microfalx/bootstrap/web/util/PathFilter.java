package net.microfalx.bootstrap.web.util;

import jakarta.servlet.http.HttpServletRequest;
import net.microfalx.lang.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static net.microfalx.lang.StringUtils.removeStartSlash;

/**
 * A utility class which filters paths based on exclusion rules.
 */
public class PathFilter {

    private final Set<String> excludedPaths = new CopyOnWriteArraySet<>();

    public PathFilter() {
        registerDefaultPaths();
    }

    public void registerExclusion(String path) {
        excludedPaths.add(removeStartSlash(path).toLowerCase());
    }

    public boolean shouldExclude(HttpServletRequest request) {
        String path = request.getRequestURI();
        return shouldExclude(path);
    }

    public boolean shouldExclude(String path) {
        path = removeStartSlash(path).toLowerCase();
        for (String excludedPath : excludedPaths) {
            if (path.startsWith(excludedPath)) return true;
        }
        return false;
    }

    public boolean shouldInclude(HttpServletRequest request) {
        String path = request.getRequestURI();
        return shouldInclude(path);
    }

    public boolean shouldInclude(String path) {
        return !shouldExclude(path);
    }

    public static String getRequestPattern(HttpServletRequest request) {
        String matchedPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (matchedPattern == null) matchedPattern = getRootPath(request.getRequestURI());
        return matchedPattern;
    }

    private void registerDefaultPaths() {
        registerExclusion("ping");
        registerExclusion("status");
        registerExclusion("event");
        registerExclusion("asset");
        registerExclusion("image");
        registerExclusion("error");
        registerExclusion("favicon.ico");
    }

    private static String getRootPath(String path) {
        return StringUtils.split(path, "/")[0];
    }
}
