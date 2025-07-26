package net.microfalx.bootstrap.content;

import net.microfalx.lang.annotation.Provider;

import java.io.IOException;

@Provider
public class TestContentPersister implements ContentPersister {

    @Override
    public void persist(Content content) throws IOException {
        // accepts everything
    }

    @Override
    public boolean supports(ContentLocator locator) {
        return locator.getUri().toASCIIString().endsWith("1.txt");
    }
}
