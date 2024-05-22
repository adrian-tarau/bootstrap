package net.microfalx.bootstrap.web.chart;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.ToString;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.TextUtils.insertSpaces;

@Data
@ToString
@JsonSerialize(using = Function.FunctionSerializer.class)
public class Function {

    private Collection<String> arguments = Collections.emptyList();
    private String body;
    private String name;

    public static Function name(String name) {
        requireNonNull(name);
        return new Function().setName(name);
    }

    public static Function body(String body) {
        requireNonNull(body);
        return new Function().setBody(body);
    }

    public static Function body(String body, String... argumentNames) {
        requireNonNull(body);
        return new Function().setBody(body).setArguments(Arrays.asList(argumentNames));
    }

    public static class FunctionSerializer extends JsonSerializer<Function> {

        @Override
        public void serialize(Function value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            StringBuilder builder = new StringBuilder();
            int indent = 0;
            if (value.name != null) {
                builder.append(value.name);
            } else {
                indent = 8;
                builder.append("function(");
                if (!value.arguments.isEmpty()) {
                    builder.append(String.join(", ", value.arguments));
                }
                builder.append(") {\n");
                builder.append(insertSpaces(value.body, 2, true));
                builder.append("\n}");
            }
            String text = indent > 0 ? insertSpaces(builder.toString(), 8) : builder.toString();
            gen.writeString(text);
        }
    }

}
