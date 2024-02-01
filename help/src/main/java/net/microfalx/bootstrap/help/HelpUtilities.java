package net.microfalx.bootstrap.help;

import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;

/**
 * Various utilities around help.
 */
public class HelpUtilities {

    private static final String RESOURCE_PATH = "/help";

    /**
     * Resolves the help path to a resource;
     *
     * @param path the path
     * @return the resource
     */
    public Resource resolve(String path) {
        String fullPath = RESOURCE_PATH + "/" + path;
        return ClassPathResource.file(fullPath + ".md");
    }
}
