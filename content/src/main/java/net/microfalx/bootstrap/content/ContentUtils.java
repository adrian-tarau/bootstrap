package net.microfalx.bootstrap.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Various utilities around content processing.
 */
public class ContentUtils {

    public static final String TEXT_BASE = "text";

    private static final int MAX_LINE_LENGTH = 160;
    private static final String INSERT = "...";
    private static final char NEW_LINE = '\n';
    private static final char SPACE = ' ';

    public static final Attributes EMPTY_ATTRIBUTES = new ContentAttributes();

    /**
     * Creates an object mapper used to parse JSON content.
     * <p>
     * The parser is as relaxed as possible.
     *
     * @return a non-null instance
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    /**
     * Normalizes the extracted content by removing a lot of empty lines which are not useful for the
     * end user.
     *
     * @param text the text
     * @return the normalized text
     */
    public static String removeRedundantNewLines(String text) {
        StringBuilder builder = new StringBuilder();
        char prevChar = NEW_LINE;
        char[] charArray = text.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == NEW_LINE) {
                if (!(prevChar == NEW_LINE || prevChar == SPACE)) builder.append(c);
            } else {
                builder.append(c);
            }
            prevChar = c;
        }
        return builder.toString();
    }

    /**
     * Returns a collection of URLs pointing to content descriptors.
     *
     * @return a non-null collection;
     */
    static Collection<URL> getContentDescriptors() throws IOException {
        Collection<URL> urls = new ArrayList<>();
        Enumeration<URL> resources = ContentUtils.class.getClassLoader().getResources("content.xml");
        while (resources.hasMoreElements()) {
            urls.add(resources.nextElement());
        }
        return urls;
    }

}
