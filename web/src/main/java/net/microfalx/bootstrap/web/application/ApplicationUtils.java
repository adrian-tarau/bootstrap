package net.microfalx.bootstrap.web.application;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

public class ApplicationUtils {

    public static String NO_VERSION = "0.0.0";

    /**
     * Returns a collection of URLs pointing to asset descriptors.
     *
     * @return a non-null collection;
     */
    static Collection<URL> getAssetDescriptors() throws IOException {
        Collection<URL> urls = new ArrayList<>();
        Enumeration<URL> resources = ApplicationUtils.class.getClassLoader().getResources("asset.xml");
        while (resources.hasMoreElements()) {
            urls.add(resources.nextElement());
        }
        return urls;
    }


}
