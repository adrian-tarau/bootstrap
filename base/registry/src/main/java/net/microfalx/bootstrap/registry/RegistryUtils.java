package net.microfalx.bootstrap.registry;

import net.microfalx.lang.FileUtils;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.StringUtils;

import static net.microfalx.lang.StringUtils.defaultIfEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;
import static net.microfalx.lang.UriUtils.SLASH;

/**
 * Various utilities around registry
 */
public class RegistryUtils {

    private RegistryUtils() {
    }

    /**
     * Returns the parent path.
     *
     * @param path the path
     * @return a non-null instance
     */
    public static String getParent(String path) {
        return defaultIfEmpty(FileUtils.getParentPath(path), SLASH);
    }

    /**
     * Normalizes a path in registry.
     *
     * @param path the path
     * @return normalized path
     */
    public static String normalizePath(String path) {
        if (StringUtils.isEmpty(path)) return SLASH;
        String[] parts = StringUtils.split(path, SLASH);
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            if (StringUtils.isEmpty(part)) continue;
            parts[index] = toIdentifier(part);
        }
        return SLASH + String.join(SLASH, parts);
    }

    /**
     * Returns the natural identifier for a path.
     * <p>
     * The path should be normalized before the natural id (the hash) is calculated
     *
     * @param path a non-null instance
     * @return a non-null instance
     */
    public static String getNaturalId(String path) {
        if (StringUtils.isEmpty(path)) path = SLASH;
        return Hashing.hash(path) + Long.toString(path.length(), Character.MAX_RADIX);
    }
}
