package net.microfalx.bootstrap.content;

import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;

import java.io.IOException;

@Provider
public class TestContentRenderer implements ContentRenderer {

    @Override
    public Resource view(Content content) throws IOException {
        return MemoryResource.create("view:" + content.getResource().loadAsString());
    }

    @Override
    public Resource edit(Content content) throws IOException {
        return MemoryResource.create("edit:" + content.getResource().loadAsString());
    }

    @Override
    public boolean supports(Content content) {
        return content.getUri().toASCIIString().endsWith("1.txt");
    }
}
