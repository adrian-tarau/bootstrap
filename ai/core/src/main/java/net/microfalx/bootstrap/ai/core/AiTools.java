package net.microfalx.bootstrap.ai.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import net.microfalx.bootstrap.ai.api.Tool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.util.json.JsonParser;

import java.util.ArrayList;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A class which holds the implementation of a few tools.
 */
class AiTools {

    private static final SchemaGenerator SUBTYPE_SCHEMA_GENERATOR;

    /**
     * Creates a tool from a given Spring AI tool callback.
     *
     * @param callback the callback
     * @return the tool
     */
    static Tool fromToolCallback(ToolCallback callback) {
        requireNonNull(callback);
        ToolDefinition toolDefinition = callback.getToolDefinition();
        Tool.Builder builder = (Tool.Builder) Tool.builder(toolDefinition.name()).description(toolDefinition.description());
        builder.executor(new ToolCallbackTool(callback));
        return builder.build();
    }

    /**
     * Returns the Spring AI tool callback from a given tool, if it exists.
     *
     * @param tool the tool
     * @return the callback, null if it does not apply
     */
    static ToolCallback callbackFromTool(Tool tool) {
        requireNonNull(tool);
        Tool.Executor executor = tool.getExecutor();
        if (executor instanceof ToolCallbackTool) {
            return ((ToolCallbackTool) executor).callback;
        } else {
            return null;
        }
    }

    /**
     * Generates a JSON schema for the given tool.
     *
     * @param tool the tool
     * @return a non-null string
     */
    static String generateSchema(Tool tool) {
        ObjectNode schema = JsonParser.getObjectMapper().createObjectNode();
        schema.put("$schema", SchemaVersion.DRAFT_2020_12.getIdentifier());
        schema.put("type", "object");

        ObjectNode properties = schema.putObject("properties");
        List<String> required = new ArrayList<>();

        for (Tool.Parameter parameter : tool.getParameters().values()) {
            if (parameter.isRequired()) required.add(parameter.getName());
            ObjectNode parameterNode = SUBTYPE_SCHEMA_GENERATOR.generateSchema(parameter.getDataType());
            // Remove OpenAPI format as some LLMs (like Mistral) don't handle them.
            parameterNode.remove("format");
            String parameterDescription = parameter.getDescription();
            if (isNotEmpty(parameterDescription)) {
                parameterNode.put("description", parameterDescription);
            }
            properties.set(parameter.getName(), parameterNode);
        }
        var requiredArray = schema.putArray("required");
        required.forEach(requiredArray::add);

        schema.put("additionalProperties", false);
        return schema.toPrettyString();
    }

    private static class ToolCallbackTool implements Tool.Executor {

        private final ToolCallback callback;

        public ToolCallbackTool(ToolCallback callback) {
            this.callback = callback;
        }

        @Override
        public Tool.ExecutionResponse execute(Tool.ExecutionRequest request) {
            callback.call("text");
            return null;
        }
    }

    static {
        Module jacksonModule = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
        SchemaGeneratorConfigBuilder schemaGeneratorConfigBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
                .with(jacksonModule)
                .with(Option.EXTRA_OPEN_API_FORMAT_VALUES)
                .with(Option.PLAIN_DEFINITION_KEYS);

        SchemaGeneratorConfig subtypeSchemaGeneratorConfig = schemaGeneratorConfigBuilder
                .without(Option.SCHEMA_VERSION_INDICATOR)
                .build();
        SUBTYPE_SCHEMA_GENERATOR = new SchemaGenerator(subtypeSchemaGeneratorConfig);
    }
}
