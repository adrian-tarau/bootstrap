package net.microfalx.bootstrap.content;

import net.microfalx.resource.Resource;

import java.io.IOException;
import java.io.Reader;

/**
 * An extractor which make binary file "printable"
 */
public class BinaryContentExtractor {

    private static final int MAXIMUM_LINE_WIDTH = 160;
    private static final char UNPRINTABLE_CHAR = '.';

    private final Resource resource;
    private StringBuilder builder = new StringBuilder();

    public BinaryContentExtractor(Resource resource) {
        this.resource = resource;
    }

    /**
     * Scans the context and extracts printable characters.
     *
     * @return the content as text
     */
    public String execute() throws IOException {
        builder.setLength(0);
        Reader reader = resource.getReader();
        int lineLength = 0;
        for (; ; ) {
            int c = reader.read();
            if (c == -1) break;
            if (lineLength >= MAXIMUM_LINE_WIDTH) {
                builder.append('\n');
                lineLength = 0;
            }
            if (isPrintable((char) c)) {
                builder.append(c);
            } else {
                builder.append(UNPRINTABLE_CHAR);
            }
            lineLength++;
        }
        return builder.toString();
    }

    private boolean isPrintable(char c) {
        return Character.isWhitespace(c) || Character.isLetterOrDigit(c);
    }
}
