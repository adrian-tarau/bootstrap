package net.microfalx.bootstrap.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A JSON parser/writer for Rest API.
 */
public class RestApiJson<T> {

    private final Class<T> type;
    private final ObjectMapper mapper = new ObjectMapper();

    public static final RestApiJson<Object> DEFAULT = create();

    /**
     * Creates a JSON mapper for generic objects.
     *
     * @return a non-null instance
     */
    public static RestApiJson<Object> create() {
        return new RestApiJson<>(Object.class);
    }

    /**
     * Creates a JSON mapper for a given type.
     *
     * @param type the type
     * @param <T>  the type of the object
     * @return a non-null instance
     */
    public static <T> RestApiJson<T> create(Class<T> type) {
        return new RestApiJson<>(type);
    }

    private RestApiJson(Class<T> type) {
        requireNonNull(type);
        this.type = type;
        setup();
    }

    /**
     * Writes the object as a JSON to a writer.
     *
     * @param writer the writer
     * @param value  the value
     * @throws IOException
     */
    public void write(Writer writer, T value) throws IOException {
        mapper.writeValue(writer, value);
    }

    /**
     * Writes the object as a JSON to a stream.
     *
     * @param outputStream the output stream
     * @param value        the value
     * @throws IOException
     */
    public void write(OutputStream outputStream, T value) throws IOException {
        mapper.writeValue(outputStream, value);
    }

    /**
     * Writes the object as a JSON.
     *
     * @param value the value
     * @return the value as JSON
     * @throws IOException
     */
    public String asString(T value) throws IOException {
        return mapper.writeValueAsString(value);
    }

    private void setup() {
        mapper.registerModule(new JavaTimeModule());
    }
}
