package net.microfalx.bootstrap.search;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.detect.CompositeEncodingDetector;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.detect.NonDetectingEncodingDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.html.charsetdetector.StandardHtmlEncodingDetector;
import org.apache.tika.sax.ToTextContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which extracts texts from a document.
 */
public class TextExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentMapper.class);

    private static final int MAX_LINE_LENGTH = 160;
    private static final String INSERT = "...";

    private final Document document;
    private final Resource body;
    private boolean forDisplay;

    public TextExtractor(Resource body) {
        requireNonNull(body);
        this.document = null;
        this.body = body;
    }

    public TextExtractor(Document document) {
        requireNonNull(document);
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
        if (MimeType.TEXT_HTML.equals(mimeType)) {
            extractTextFromHtml(builder, mimeType);
        } else if (MimeType.APPLICATION_JSON.equals(mimeType)) {
            extractTextFromJson(builder, mimeType);
        } else {
            builder.append(body.loadAsString());
        }
        return builder.toString();
    }

    private void extractTextFromHtml(StringBuilder builder, MimeType mimeType) throws IOException {
        HtmlParser parser = new HtmlParser(createEncodingDetector(mimeType));
        StringWriter sw = new StringWriter();
        try {
            parser.parse(body.getInputStream(), new ToTextContentHandler(sw), new Metadata(), new ParseContext());
            builder.append(sw);
        } catch (Exception e) {
            // any failure, log and just return the content as is
            LOGGER.warn("Failed to parse HTML document: " + document.getBodyUri() + ", root cause: " + e.getMessage());
        }
    }

    private void extractTextFromJson(StringBuilder builder, MimeType mimeType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        JsonNode jsonNode = objectMapper.readTree(body.getReader());
        AtomicInteger lineLength = new AtomicInteger();
        walkTree(jsonNode, (k, v) -> {
            builder.append(k).append(' ').append(v).append(' ');
            lineLength.addAndGet(k.length());
            lineLength.addAndGet(v.length());
            if (lineLength.get() > MAX_LINE_LENGTH) {
                builder.append('\n');
                lineLength.set(0);
            }
        });
    }

    public void walkTree(JsonNode root, BiConsumer<String, String> consumer) {
        walker(null, root, consumer);
    }

    private void walker(String nodename, JsonNode node, BiConsumer<String, String> consumer) {
        String nameToPrint = nodename != null ? nodename : "must_be_root";
        if (node.isObject()) {
            node.fields().forEachRemaining(e -> walker(e.getKey(), e.getValue(), consumer));
        } else if (node.isArray()) {
            node.elements().forEachRemaining(n -> walker("array item of '" + nameToPrint + "'", n, consumer));
        } else {
            if (node.isValueNode()) {
                consumer.accept(nameToPrint, node.asText());
            } else {
                throw new IllegalStateException("Node must be one of value, array or object.");
            }
        }
    }

    private EncodingDetector createEncodingDetector(MimeType mimeType) {
        List<EncodingDetector> encodingDetectors = new ArrayList<>();
        if (MimeType.TEXT_HTML.equals(mimeType)) encodingDetectors.add(new StandardHtmlEncodingDetector());
        encodingDetectors.add(new NonDetectingEncodingDetector());
        return new CompositeEncodingDetector(encodingDetectors);
    }
}
