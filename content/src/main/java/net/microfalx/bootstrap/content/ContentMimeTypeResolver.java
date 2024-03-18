package net.microfalx.bootstrap.content;

import net.microfalx.lang.annotation.Order;
import net.microfalx.resource.MimeTypeResolver;
import net.microfalx.resource.StreamResource;

import java.io.IOException;
import java.io.InputStream;

@Order(Order.BEFORE)
public class ContentMimeTypeResolver implements MimeTypeResolver {

    ContentService contentService;

    @Override
    public String detect(InputStream inputStream, String fileName) throws IOException {
        return contentService.detectMimeType(StreamResource.create(inputStream, fileName)).toString();
    }
}
