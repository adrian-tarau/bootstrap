package net.microfalx.bootstrap.content.impl;

import net.microfalx.bootstrap.content.BinaryContentExtractor;
import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentException;
import net.microfalx.bootstrap.content.ContentRenderer;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;

import java.io.IOException;

@Provider
public class BinaryContentRenderer implements ContentRenderer {

    @Override
    public Resource view(Content content) throws IOException {
        Resource resource = content.getResource();
        if (resource.exists()) {
            BinaryContentExtractor extractor = new BinaryContentExtractor(content.getResource());
            return MemoryResource.create(extractor.execute()).withName(content.getName());
        } else {
            return resource;
        }
    }

    @Override
    public Resource edit(Content content) throws IOException {
        Resource resource = content.getResource();
        if (resource.exists()) {
            throw new ContentException("Binary content cannot be edited: " + content.getUri());
        } else {
            return resource;
        }
    }

    @Override
    public boolean supports(Content content) {
        return MimeType.APPLICATION_OCTET_STREAM.equals(content.getMimeType());
    }
}
