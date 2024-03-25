package net.microfalx.bootstrap.content;

import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Extracts all the text.
 */
public class ContentExtractor extends DefaultHandler {

    private final StringBuilder builder = new StringBuilder();
    private final Map<String, Set<String>> attributes = new HashMap<>();

    private String lastElement;
    private StringBuilder elementBody = new StringBuilder();
    private int maximumElementLength = 50;
    private boolean extractAttributes;

    public int getMaximumElementLength() {
        return maximumElementLength;
    }

    public void setMaximumElementLength(int maximumElementLength) {
        this.maximumElementLength = maximumElementLength;
    }

    public boolean isExtractAttributes() {
        return extractAttributes;
    }

    public void setExtractAttributes(boolean extractAttributes) {
        this.extractAttributes = extractAttributes;
    }

    public net.microfalx.bootstrap.model.Attributes<?> getAttributes() {
        if (!extractAttributes || attributes.isEmpty()) return net.microfalx.bootstrap.model.Attributes.empty();
        net.microfalx.bootstrap.model.Attributes<Attribute> finalAttributes = net.microfalx.bootstrap.model.Attributes.create();
        for (Map.Entry<String, Set<String>> entry : attributes.entrySet()) {
            if (entry.getValue().size() == 1) {
                String value = entry.getValue().iterator().next();
                if (acceptAttributeValue(value)) finalAttributes.add(entry.getKey(), value);
            }
        }
        return finalAttributes;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (extractAttributes) {
            lastElement = StringUtils.defaultIfEmpty(localName, qName);
            elementBody.setLength(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        builder.append(ch, start, length);
        if (extractAttributes) {
            elementBody.append(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        super.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (extractAttributes && isNotEmpty(lastElement)) {
            String value = elementBody.toString();
            if (value.length() < maximumElementLength && isNotEmpty(value)) {
                Set<String> values = attributes.computeIfAbsent(lastElement, s -> new HashSet<>());
                values.add(value);
            }
        }
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    private boolean acceptAttributeValue(String value) {
        if (isIsoDateTime(value)) return false;
        return true;
    }

    private boolean isIsoDateTime(String value) {
        boolean isoDateTime = false;
        try {
            DateTimeFormatter.ISO_INSTANT.parse(value);
            isoDateTime = true;
        } catch (DateTimeParseException e) {
            // ignore
        }
        return isoDateTime;
    }
}
