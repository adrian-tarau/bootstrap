package net.microfalx.bootstrap.content.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.bootstrap.content.ContentUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public class JsonParser extends AbstractParser {

    private static final char[] NEW_LINE_CHAR_ARRAY = "\n".toCharArray();
    private static final Set<MediaType> MEDIA_TYPES = new HashSet<>();

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return unmodifiableSet(MEDIA_TYPES);
    }

    @Override
    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {
        ObjectMapper objectMapper = ContentUtils.createObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(stream);
        walkTree(null, jsonNode, handler);
    }

    private void walkTree(String name, JsonNode node, ContentHandler handler) throws IOException, SAXException {
        String nameToPrint = name != null ? name : "must_be_root";
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                handler.startElement(null, entry.getKey(), entry.getKey(), ContentUtils.EMPTY_ATTRIBUTES);
                walkTree(entry.getKey(), entry.getValue(), handler);
                handler.endElement(null, entry.getKey(), entry.getKey());
                writeNewLine(handler);
            }
        } else if (node.isArray()) {
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext()) {
                JsonNode childNode = elements.next();
                walkTree(nameToPrint, childNode, handler);
                writeNewLine(handler);
            }
        } else {
            if (node.isValueNode()) {
                char[] chars = node.asText().toCharArray();
                handler.characters(chars, 0, chars.length);
            } else {
                throw new IllegalStateException("Node must be one of value, array or object.");
            }
        }
    }

    private void writeNewLine(ContentHandler handler) throws SAXException {
        handler.characters(NEW_LINE_CHAR_ARRAY, 0, NEW_LINE_CHAR_ARRAY.length);
    }

    static {
        MEDIA_TYPES.add(MediaType.parse("application/json"));
        MEDIA_TYPES.add(MediaType.parse("text/json"));
    }
}
