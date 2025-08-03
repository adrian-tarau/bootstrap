package net.microfalx.bootstrap.help;

import net.microfalx.lang.StringUtils;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.StringUtils.removeEndSlash;
import static net.microfalx.lang.StringUtils.removeStartSlash;

/**
 * Various utilities around help.
 */
public class HelpUtilities {

    public static final String DOCUMENT_OWNER = "help";
    public static final String DOCUMENT_TYPE = "toc";
    public static final String PATH_FIELD = "path";
    public static final String RESOURCE_PATH = "help";

    public static final String MARKDOWN_EXTENSION = "md";
    public static final String HTML_EXTENSION = "html";

    /**
     * Resolves the path to a help related resource
     *
     * @param path the path
     * @return the resource
     */
    public static String resolvePath(String path) {
        return "/" + RESOURCE_PATH + "/" + removeStartSlash(removeEndSlash(path));
    }

    /**
     * Resolves the help path to a resource;
     *
     * @param path the path
     * @return the resource
     */
    public static String resolveContentPath(String path) {
        return resolvePath(path) + ".md";
    }

    /**
     * Resolves the content behind the help path.
     *
     * @param path the path
     * @return the resource
     */
    public static Resource resolveContent(String path) {
        String fullPath = resolveContentPath(path);
        return ClassPathResource.file(fullPath).withMimeType(MimeType.TEXT_MARKDOWN);
    }

    /**
     * Resolves the image behind the help path.
     *
     * @param path the path
     * @return the resource
     */
    public static Resource resolveImage(String path) {
        String fullPath = resolvePath(path);
        return ClassPathResource.file(fullPath);
    }

    /**
     * Returns the anchor ID for a given path.
     *
     * @param path the path of a TOC entry
     * @return the anchor ID
     */
    public static String getAnchorId(String path) {
        if (path == null) path = "root";
        return "_" + StringUtils.toIdentifier(path);
    }
}
