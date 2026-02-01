package net.microfalx.bootstrap.web.util;

import jakarta.servlet.http.HttpServletRequest;
import net.microfalx.lang.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.UriUtils.SLASH;

/**
 * A utility class which filters paths based on exclusion rules.
 */
public class PathFilter {

    private final Set<String> excludedPaths = new CopyOnWriteArraySet<>();

    public PathFilter() {
        this(true);
    }

    public PathFilter(boolean withDefaults) {
        if (withDefaults) registerDefaultPaths();
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
        requireNonNull(request);
        String matchedPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (matchedPattern == null) matchedPattern = getRootPath(request.getRequestURI(), -1);
        return addStartSlash(matchedPattern);
    }

    /**
     * Returns the root path with the given number of parts.
     *
     * @param request the request
     * @return a non-null instance
     */
    public static String getRootPath(HttpServletRequest request) {
        return getRootPath(request, -1);
    }

    /**
     * Returns the root path with the given number of parts.
     *
     * @param request the request
     * @param parts   the number of parts
     * @return a non-null instance
     */
    public static String getRootPath(HttpServletRequest request, int parts) {
        requireNonNull(request);
        return getRootPath(request.getRequestURI(), parts);
    }

    private void registerDefaultPaths() {
        registerExclusion("ping");
        registerExclusion("status");
        registerExclusion("event");
        registerExclusion("asset");
        registerExclusion("image");
        registerExclusion("error");
        registerExclusion("favicon.ico");
        registerExclusion(".well-known");
    }

    private static String getRootPath(String path, int parts) {
        String[] fragments = StringUtils.split(path, "/");
        int finalParts = parts;
        if (finalParts <= 0) {
            // if in auto mode, if it is longer than 3
            if (fragments.length >= 3) {
                finalParts = 2;
            } else {
                finalParts = 1;
            }
        }
        return defaultIfEmpty(StringUtils.join("/", Arrays.copyOf(fragments, finalParts)), SLASH);
    }
}
