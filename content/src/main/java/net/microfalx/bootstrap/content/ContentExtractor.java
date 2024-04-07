package net.microfalx.bootstrap.content;

import net.microfalx.bootstrap.model.Attribute;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static net.microfalx.lang.StringUtils.*;

/**
 * Extracts all the text.
 */
public class ContentExtractor extends DefaultHandler {

    private final StringBuilder builder = new StringBuilder();
    private final Map<String, Set<String>> attributes = new HashMap<>();

    private Stack<String> elements = new Stack<>();
    private StringBuilder elementBody = new StringBuilder();
    private boolean extractAttributes;

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
            String name = defaultIfEmpty(localName, qName);
            elements.push(name);
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
        if (!extractAttributes) return;
        String name = calculateAttributeName();
        if (!elements.isEmpty()) elements.pop();
        if (isEmpty(name)) return;
        String value = elementBody.toString();
        if (containsWhiteSpacesOnly(value)) return;
        Set<String> values = attributes.computeIfAbsent(name, s -> new HashSet<>());
        values.add(value);
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    private String calculateAttributeName() {
        if (elements.isEmpty()) return EMPTY_STRING;
        StringBuilder attributeNameBuilder = new StringBuilder();
        Iterator<String> iterator = elements.iterator();
        attributeNameBuilder.append(iterator.next());
        while (iterator.hasNext()) {
            String part = iterator.next();
            attributeNameBuilder.append(capitalizeFirst(part));
        }
        return attributeNameBuilder.toString();
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
