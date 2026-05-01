package net.microfalx.bootstrap.configuration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import static net.microfalx.lang.StringUtils.*;

/**
 * Various utilities around configuration.
 */
public class ConfigurationUtils {

    protected final static char SEPARATOR = '.';
    protected final static String ROOT_METADATA_ID = "ROOT";
    protected final static String REGISTRY_PATH = "/configuration";

    /**
     * Returns the parent key.
     *
     * @param key the key
     * @return the parent key, empty string if there is no parent
     */
    public static String getParent(String key) {
        if (isEmpty(key)) return EMPTY_STRING;
        int index = key.lastIndexOf(SEPARATOR);
        if (index == -1) return EMPTY_STRING;
        return key.substring(0, index);
    }

    /**
     * Returns the last component of the key.
     *
     * @param key the key
     * @return the key, empty string if there is no parent
     */
    public static String getLast(String key) {
        if (isEmpty(key)) return EMPTY_STRING;
        if (key.charAt(0) == SEPARATOR) key = key.substring(1);
        if (isEmpty(key)) return EMPTY_STRING;
        if (key.charAt(key.length() - 1) == SEPARATOR) key = key.substring(0, key.length() - 1);
        if (key.length() == 1 && key.charAt(0) == SEPARATOR) return EMPTY_STRING;
        int index = key.lastIndexOf(SEPARATOR);
        if (index == -1) return key;
        return key.substring(index + 1);
    }

    /**
     * Returns a title extracted from the last component of a configuration key.
     *
     * @param key the configuration key
     * @return the title
     */
    public static String getTitle(String key) {
        if (isEmpty(key)) return NA_STRING;
        String last = getLast(key);
        if (isEmpty(last)) return NA_STRING;
        return beautifyCamelCase(last);
    }

    /**
     * Returns a collection of URLs pointing to configuration descriptors.
     *
     * @return a non-null collection;
     */
    static Collection<URL> getDescriptors() throws IOException {
        Collection<URL> urls = new ArrayList<>();
        Enumeration<URL> resources = ConfigurationUtils.class.getClassLoader().getResources("configuration.xml");
        while (resources.hasMoreElements()) {
            urls.add(resources.nextElement());
        }
        return urls;
    }
}
