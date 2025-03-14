package net.microfalx.bootstrap.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;

import static net.microfalx.bootstrap.model.AttributeConstants.MAX_ATTRIBUTE_DISPLAY_LENGTH;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Various utilities around attributes
 */
public class AttributeUtils {

    /**
     * A set containing all the core field names
     */
    private static final Map<String, Integer> ATTRIBUTE_PRIORITY = new HashMap<>();

    private AttributeUtils() {
    }

    /**
     * Returns whether the attribute is small enough
     *
     * @param attribute      the attribute
     * @param includeNumbers {@code    true} to allow numbers, {@code false}
     * @param <A>            the attribute type
     * @return {@code true} to allow to be displayed as a badge, {@code false} otherwise
     */
    public static <A extends Attribute> boolean shouldDisplayAsBadge(A attribute, boolean includeNumbers) {
        requireNonNull(attribute);
        if (attribute.isEmpty()) return false;
        if (!includeNumbers && attribute.isNumber()) return false;
        return attribute.isSingleLine();
    }

    /**
     * Returns whether the text is small and has a single line.
     *
     * @param text the text to test
     * @return <code>true</code> if single line and short, <code>false</code> otherwise
     */
    public static boolean isSingleLineAndShort(String text) {
        return text == null ? false : text.length() < MAX_ATTRIBUTE_DISPLAY_LENGTH && (int) text.lines().count() == 1;
    }

    /**
     * Encodes the attributes into a Java properties text.
     *
     * @param attributes the attributes
     * @return the resource
     */
    public static <A extends Attribute> Resource encodeProperties(Attributes<A> attributes) {
        requireNonNull(attributes);
        Properties properties = new Properties();
        for (A attribute : attributes.toCollection()) {
            properties.put(attribute.getName(), ObjectUtils.toString(attribute.getValue()));
        }
        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, null);
        } catch (IOException e) {
            // it will never happen since it is in memory
        }
        return MemoryResource.create(writer.toString()).withMimeType(MimeType.APPLICATION_JSON);
    }

    /**
     * Decodes the attributes from a property file content.
     *
     * @param resource the JSON as a resource
     * @return the attributes
     */
    public static <A extends Attribute> Attributes<A> decodeProperties(Resource resource) throws IOException {
        Attributes<A> attributes = Attributes.create();
        return decodeProperties(resource, attributes);
    }

    /**
     * Decodes the attributes from a property file content.
     *
     * @param resource the JSON as a resource
     * @return the attributes
     */
    public static <A extends Attribute> Attributes<A> decodeProperties(Resource resource, Attributes<A> attributes) throws IOException {
        requireNonNull(resource);
        requireNonNull(attributes);
        if (!resource.exists()) return attributes;
        Properties properties = new Properties();
        properties.load(resource.getInputStream());
        properties.forEach((k, v) -> attributes.add(k.toString(), v));
        return attributes;
    }

    /**
     * Encodes the attributes into a JSON.
     *
     * @param attributes the attributes
     * @return the resource
     */
    public static <A extends Attribute> Resource encodeJson(Attributes<A> attributes) {
        requireNonNull(attributes);
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            objectMapper.writeValue(writer, attributes.toMap().values());
        } catch (IOException e) {
            // it will never happen since it is in memory
        }
        return MemoryResource.create(writer.toString()).withMimeType(MimeType.APPLICATION_JSON);
    }

    /**
     * Decodes the attributes from a JSON content.
     *
     * @param resource the JSON as a resource
     * @return the attributes
     */
    public static <A extends Attribute> Attributes<A> decodeJson(Resource resource) throws IOException {
        Attributes<A> attributes = Attributes.create();
        return decodeJson(resource, attributes);
    }

    /**
     * Decodes the attributes from a JSON content.
     *
     * @param resource the JSON as a resource
     * @return the attributes
     */
    public static <A extends Attribute> Attributes<A> decodeJson(Resource resource, Attributes<A> attributes) throws IOException {
        requireNonNull(resource);
        requireNonNull(attributes);
        if (!resource.exists()) return attributes;
        ObjectMapper objectMapper = new ObjectMapper();
        Iterator<JsonNode> nodes = objectMapper.readTree(resource.getReader()).elements();
        while (nodes.hasNext()) {
            JsonNode attributeNode = nodes.next();
            String name = attributeNode.get("name").textValue();
            JsonNode valueNode = attributeNode.get("value");
            Object value;
            if (valueNode.isFloat()) {
                value = valueNode.floatValue();
            } else if (valueNode.isDouble()) {
                value = valueNode.doubleValue();
            } else if (valueNode.isNumber()) {
                value = valueNode.longValue();
            } else {
                value = valueNode.textValue();
            }
            attributes.add(name, value);
        }
        return attributes;
    }

    /**
     * Returns a collection of attributes sorted by priority.
     * <p>
     * Some core attributes are always presented first: severity, source, target, etc
     *
     * @param attributes the attributes to sort
     * @param maximum    the maximum number of attributes to be left out after sorting
     * @return a non-null instance with sorted attributes
     */
    @SuppressWarnings("CastCanBeRemovedNarrowingVariableType")
    public static <A extends Attribute> Collection<A> sortAndFilter(Collection<A> attributes, int maximum, Function<A, Boolean> filter) {
        if (attributes == null) return Collections.emptyList();
        if (filter == null) filter = a -> true;
        List<A> priorityAttributes = new ArrayList<>();
        List<A> sortedAttributes = new ArrayList<>();
        Iterator<A> attributeIterator = attributes.iterator();
        while (attributeIterator.hasNext()) {
            A attribute = attributeIterator.next();
            if (!filter.apply(attribute)) continue;
            if (ATTRIBUTE_PRIORITY.containsKey(attribute.getName().toLowerCase())) {
                priorityAttributes.add(attribute);
            } else {
                sortedAttributes.add(attribute);
            }
        }
        sortedAttributes.sort(ATTRIBUTE_COMPARATOR);
        priorityAttributes.sort(ATTRIBUTE_COMPARATOR);
        priorityAttributes.addAll(sortedAttributes);
        while (priorityAttributes.size() > maximum) {
            priorityAttributes.remove(priorityAttributes.size() - 1);
        }
        return priorityAttributes;
    }

    /**
     * Returns the priority for an attribute.
     *
     * @param name the name
     * @return the priority, null if it does not have a fixed priority
     */
    public static Integer getAttributePriority(String name) {
        requireNonNull(name);
        return ATTRIBUTE_PRIORITY.get(name.toLowerCase());
    }

    /**
     * Registers an attribute priority.
     * <p>
     * Lower is higher priority).
     *
     * @param name     the attribute name
     * @param priority the priority between 0 and 1000
     */
    public static void registerAttributePriority(String name, int priority) {
        requireNonNull(name);
        ATTRIBUTE_PRIORITY.put(name.toLowerCase(), priority);
    }

    /**
     * Replaces the variables in the given text.
     * <p>
     * Variables can be accessed by using the placeholder <code>${name}</code>. Parameters are case-insensitive.
     *
     * @param attributes the attributes which provide the variables
     * @param text       the text with variables
     * @return the text with all variables replaced
     */
    public static String replaceVariables(Attributes<?> attributes, String text) {
        requireNonNull(attributes);
        if (StringUtils.isEmpty(text)) return text;
        for (; ; ) {
            int startIndex = text.indexOf("${");
            if (startIndex == -1) break;
            int endIndex = text.indexOf("}", startIndex);
            String name = text.substring(startIndex + 2, endIndex);
            String value = attributes.get(name, StringUtils.EMPTY_STRING).asString();
            text = text.substring(0, startIndex) + value + text.substring(endIndex + 1);
        }
        return text;
    }

    static Comparator<Attribute> ATTRIBUTE_COMPARATOR = new AttributeComparator();

    protected static class AttributeComparator implements Comparator<Attribute> {

        @Override
        public int compare(Attribute o1, Attribute o2) {
            return o1.compareTo(o2);
        }
    }

    static {
        registerAttributePriority(Attribute.SEVERITY, -10);
    }
}
