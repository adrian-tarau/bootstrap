package net.microfalx.bootstrap.content;

import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.ClassPathResource;

import java.io.IOException;

@Provider
public class TestContentResolver implements ContentResolver {

    @Override
    public Content resolve(ContentLocator locator) throws IOException  {
        return Content.create(ClassPathResource.file("1.txt"));
    }

    @Override
    public boolean supports(ContentLocator locator) {
        return locator.getUri().toASCIIString().endsWith("1.txt");
    }
}
