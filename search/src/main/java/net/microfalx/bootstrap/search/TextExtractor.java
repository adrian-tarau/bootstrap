package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which extracts texts from a document.
 */
public class TextExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentMapper.class);

    private static final int MAX_LINE_LENGTH = 160;

    private final Document document;
    private final Resource body;
    private boolean forDisplay;

    private ContentService contentService;

    public TextExtractor(ContentService contentService, Resource body) {
        requireNonNull(contentService);
        requireNonNull(body);
        this.contentService = contentService;
        this.document = null;
        this.body = body;
    }

    public TextExtractor(ContentService contentService, Document document) {
        requireNonNull(contentService);
        requireNonNull(document);
        this.contentService = contentService;
        this.document = document;
        this.body = document.getBody();
    }

    /**
     * Returns whether the text is extracted for display purposes.
     *
     * @return {@code true} for display, {@code false} otherwise
     */
    public boolean isForDisplay() {
        return forDisplay;
    }

    /**
     * Changes whether the text is extracted for display purposes.
     *
     * @param forDisplay {@code true} for display, {@code false} otherwise
     * @return self
     */
    public TextExtractor setForDisplay(boolean forDisplay) {
        this.forDisplay = forDisplay;
        return this;
    }

    /**
     * Extracts the text behind the document based on the mime-type.
     * <p>
     * If the content is binary, an empty string is returned.
     *
     * @return the content
     * @throws IOException in case of an I/O error
     */
    public String execute() throws IOException {
        String mimeType = body.getMimeType();
        if (!MimeType.get(mimeType).isText()) {
            try {
                mimeType = body.detectMimeType();
            } catch (Exception e) {
                // ignore any failure during mime type detection
            }
        }
        if (MimeType.get(mimeType).isText()) {
            return extractText();
        } else {
            return forDisplay ? "Binary content" : StringUtils.EMPTY;
        }
    }

    private String extractText() throws IOException {
        StringBuilder builder = new StringBuilder();
        if (document != null) {
            builder.append(document.getName()).append('\n');
            if (StringUtils.isNotEmpty(document.getDescription())) {
                builder.append(document.getDescription()).append('\n');
            }
        }
        MimeType mimeType = MimeType.get(body.getMimeType());
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, mimeType.getValue());
        Content content = contentService.extract(body, false, metadata);
        builder.append(content.loadAsString());
        return builder.toString();
    }

}
