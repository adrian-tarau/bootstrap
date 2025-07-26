package net.microfalx.bootstrap.search;

import net.microfalx.lang.StringUtils;
import net.microfalx.resource.ClassPathResource;
import org.apache.lucene.util.ClasspathResourceLoader;
import org.apache.lucene.util.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * A resource loader which loads everything from class path.
 */
public class SearchResourceLoader implements ResourceLoader {

    private static final String PATH = "search";

    @Override
    public InputStream openResource(String resource) throws IOException {
        resource = StringUtils.removeStartSlash(resource);
        return ClassPathResource.create(PATH + "/" + resource).getInputStream();
    }

    @Override
    public <T> Class<? extends T> findClass(String cname, Class<T> expectedType) {
        try {
            return Class.forName(cname, true, ClasspathResourceLoader.class.getClassLoader()).asSubclass(expectedType);
        } catch (Exception e) {
            throw new SearchException("Cannot load class: " + cname, e);
        }
    }
}
