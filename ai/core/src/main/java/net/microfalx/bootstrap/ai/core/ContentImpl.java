package net.microfalx.bootstrap.ai.core;

import lombok.ToString;
import net.microfalx.bootstrap.ai.api.Content;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.springframework.ai.content.Media;

import java.io.IOException;
import java.net.URL;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.emptyIfNull;

@ToString
public class ContentImpl implements Content {

    private final Type type;
    private final Resource resource;

    static Content from(Media media) {
        requireNonNull(media);
        return new ContentImpl(getType(media), getResource(media));
    }

    public static Content from(String text) {
        text = emptyIfNull(text);
        return new ContentImpl(Content.Type.TEXT, Resource.text(text));
    }

    private ContentImpl(Type type, Resource resource) {
        requireNonNull(type);
        requireNonNull(resource);
        this.type = type;
        this.resource = resource;
    }

    @Override
    public String getName() {
        return resource.getName();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public long getSize() {
        try {
            return resource.length();
        } catch (IOException e) {
            return -1;
        }
    }

    private static Type getType(Media media) {
        MimeType mimeType = MimeType.get(media.getMimeType().getType());
        if (mimeType.isText()) {
            return Type.TEXT;
        } else if (MimeType.IMAGE.equals(mimeType)) {
            return Type.IMAGE;
        } else {
            return Type.VIDEO;
        }
    }

    private static Resource getResource(Media media) {
        Resource resource;
        if (media.getData() instanceof String) {
            resource = Resource.url((String) media.getData());
        } else if (media.getData() instanceof byte[]) {
            resource = Resource.bytes((byte[]) media.getData());
        } else {
            throw new IllegalArgumentException("Unsupported media: " + media);
        }
        MimeType mimeType = MimeType.get(media.getMimeType().getType());
        return resource.withName(media.getName()).withMimeType(mimeType);
    }

    private static Resource create(URL url, String base64Encoded) {
        if (url != null) {
            return Resource.url(url);
        } else if (base64Encoded != null) {
            return Resource.base64Encoded(base64Encoded);
        } else {
            throw new IllegalArgumentException("Either URL or base64Encoded must be provided");
        }
    }
}
