package net.microfalx.bootstrap.web.chart.series;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.ToString;

import java.io.IOException;
import java.time.Instant;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which holds a numeric value at a point in time or associated with a category.
 */
@ToString
@JsonSerialize(using = Value.XSerializer.class)
public class Value<X, Y extends Number> {

    private final X x;
    private final Y y;

    public static <X, Y extends Number> Value<X, Y> create(X x, Y y) {
        return new Value<>(x, y);
    }

    Value(X x, Y y) {
        requireNonNull(x);
        requireNonNull(y);
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the value for the X axis (the category or the timestamp).
     *
     * @return the value
     */
    public X getX() {
        return x;
    }

    /**
     * Returns the value for the Y axis (a number).
     *
     * @return the value
     */
    public Y getY() {
        return y;
    }

    public static class XSerializer extends JsonSerializer<Value<?, ?>> {

        @Override
        public void serialize(Value<?, ?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            Object x = value.x;
            if (x instanceof Instant) {
                gen.writePOJOField("x", ((Instant) x).toEpochMilli());
            } else {
                gen.writePOJOField("x", value.x);
            }
            gen.writePOJOField("y", value.y);
            gen.writeEndObject();
        }
    }
}
