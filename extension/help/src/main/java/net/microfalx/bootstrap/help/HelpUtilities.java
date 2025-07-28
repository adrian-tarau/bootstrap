package net.microfalx.bootstrap.help;

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
    public static final String RESOURCE_PATH = "/help";

    /**
     * Resolves the help path to a resource;
     *
     * @param path the path
     * @return the resource
     */
    public static String resolveResourcePath(String path) {
        return RESOURCE_PATH + "/" + removeStartSlash(removeEndSlash(path)) + ".md";
    }

    /**
     * Resolves the content behind the help path;
     *
     * @param path the path
     * @return the resource
     */
    public static Resource resolve(String path) {
        String fullPath = resolveResourcePath(path);
        return ClassPathResource.file(fullPath).withMimeType(MimeType.TEXT_MARKDOWN);
    }
}
