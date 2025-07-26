package net.microfalx.bootstrap.content.impl;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.bootstrap.content.ContentUtils;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.tika.mime.MediaType.OCTET_STREAM;

public class JsonDetector implements Detector {

    private static final int BYTES_TO_TEST = 64 * 1024;
    private static final int TOKENS_TO_TEST = 10;
    private static final MediaType APPLICATION_JSON = MediaType.parse("application/json");

    @Override
    public MediaType detect(InputStream input, Metadata metadata) throws IOException {
        if (seemsJson(input)) {
            input.mark(BYTES_TO_TEST);
            try {
                ObjectMapper objectMapper = ContentUtils.createObjectMapper();
                final JsonParser parser = objectMapper.getFactory().createParser(input);
                int tokenCount = 0;
                for (int i = 0; i < TOKENS_TO_TEST; i++) {
                    JsonToken token = parser.nextToken();
                    if (token == null) break;
                    tokenCount++;
                }
                return tokenCount > 2 ? APPLICATION_JSON : OCTET_STREAM;
            } catch (JsonParseException e) {
                // if we cannot parse, it is not a JSON
            } catch (JacksonException e) {
                // just in case, catch the root exception, maybe log since this should not happen?
            } finally {
                input.reset();
            }
        }
        return OCTET_STREAM;
    }

    /**
     * Probes the characters in the stream, looking for object or array marker since most JSON objects are not simple
     * types.
     *
     * @param input the input
     * @return {@code true} if it seems to be a JSON object, {@code false} otherwise
     * @throws IOException I/O error
     */
    private boolean seemsJson(InputStream input) throws IOException {
        input.mark(BYTES_TO_TEST);
        try {
            char c = 0x00;
            int maxChars = 1000;
            while (maxChars-- > 0) {
                c = (char) input.read();
                if (!Character.isWhitespace(c)) break;
            }
            return c == '{' || c == '[';
        } finally {
            input.reset();
        }
    }
}
