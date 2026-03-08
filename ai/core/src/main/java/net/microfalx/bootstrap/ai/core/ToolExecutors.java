package net.microfalx.bootstrap.ai.core;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Tool;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.ObjectUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A collection of utilities for tool executors.
 */
@Slf4j
public class ToolExecutors {

    private static final int MAX_TOOL_CALLS = 5;
    private static final ThreadLocal<AtomicInteger> TOOL_INVOCATIONS = ThreadLocal.withInitial(AtomicInteger::new);

    /**
     * Wraps the response from a tool execution in a standardized format.
     *
     * @param tool      the tool
     * @param input     the request sent to the tool execution
     * @param output    the response from the tool execution
     * @param throwable the exception raised after tool execution
     * @return the final response
     */
    public static String wrapResponse(Tool tool, String input, String output, Throwable throwable) {
        if (TOOL_INVOCATIONS.get().incrementAndGet() > MAX_TOOL_CALLS) {
            throw new AiException("Exceeded maximum number of tool (" + MAX_TOOL_CALLS
                    + ") calls in the same request");
        }
        StringBuilder builder = new StringBuilder();
        if (throwable != null) {
            LOGGER.atError().setCause(throwable).log("Tool '{}' failed for input: {}", tool.getId(), input);
            return "Observation: The tool failed, maybe use a different tool.";
        }
        if (isNotEmpty(output)) {
            builder.append("""
                    Observation: The tool returned the data bellow. Answer the user's original \
                    question naturally, using this data.
                    
                    """);
            builder.append(output);
        } else {
            builder.append("Observation: The tool '").append(tool.getName())
                    .append("' returned not data, use a different tool.");
        }
        return builder.toString();
    }

    /**
     * Creates an execution request for a given tool based on the
     *
     * @param chat  the chat for which the request is built
     * @param tool  the tool for which the request is built
     * @param input the input as a JSON
     * @return the execution request
     */
    public static Tool.ExecutionRequest createRequest(Chat chat, Tool tool, String input) {
        requireNonNull(chat);
        requireNonNull(tool);
        Map<?, ?> parameterValues = Field.from(input, Map.class);
        Map<String, Object> arguments = new HashMap<>();
        Map<String, Tool.Parameter> parameters = tool.getParameters();
        for (Map.Entry<String, Tool.Parameter> entry : parameters.entrySet()) {
            Object value = parameterValues.get(entry.getKey());
            arguments.put(entry.getKey(), value);
        }
        TOOL_INVOCATIONS.remove();
        return new ExecutionRequestImpl(chat, tool, arguments);

    }

    static class ToolCallbackImpl implements ToolCallback {

        private final Chat chat;
        private final Tool tool;

        ToolCallbackImpl(Chat chat, Tool tool) {
            requireNonNull(chat);
            requireNonNull(tool);
            this.chat = chat;
            this.tool = tool;
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return ToolDefinition.builder().name(tool.getName()).description(tool.getDescription()).inputSchema(AiTools.generateSchema(tool)).build();
        }

        @Override
        public String call(String toolInput) {
            Tool.ExecutionRequest request = null;
            try {
                request = ToolExecutors.createRequest(chat, tool, toolInput);
                Tool.ExecutionResponse response = tool.getExecutor().execute(request);
                ((AbstractChat) chat).registerToolExecution(request, response);
                return response.getContent().getResource().loadAsString();
            } catch (Exception e) {
                if (request != null) ((AbstractChat) chat).registerToolExecution(request, e);
                LOGGER.atError().setCause(e).log("Tool '{}' failed for input", toolInput);
                return "Observation: The tool failed, maybe use a different tool.";
            }
        }

    }

    @ToString
    static class ExecutionRequestImpl implements Tool.ExecutionRequest {

        private final Chat chat;
        private final Tool tool;
        private final String id = UUID.randomUUID().toString();
        private final Map<String, Object> arguments;
        private final LocalDateTime timestamp = LocalDateTime.now();

        ExecutionRequestImpl(Chat chat, Tool tool, Map<String, Object> arguments) {
            requireNonNull(chat);
            requireNonNull(tool);
            this.chat = chat;
            this.tool = tool;
            this.arguments = ObjectUtils.defaultIfNull(arguments, Collections.emptyMap());
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return tool.getName();
        }

        @Override
        public String getDescription() {
            StringBuilder sb = new StringBuilder();
            sb.append(tool.getName()).append("(");
            if (ObjectUtils.isNotEmpty(arguments)) {
                arguments.forEach((key, value) -> sb.append(key).append("=").append(value).append(", "));
                sb.setLength(sb.length() - 2); // Remove last comma and space
            }
            sb.append(")");
            return sb.toString();
        }

        @Override
        public Chat getChat() {
            return chat;
        }

        @Override
        public Tool getTool() {
            return tool;
        }

        @Override
        public Map<String, Object> getArguments() {
            return unmodifiableMap(arguments);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getArgument(String name) {
            return (T) arguments.get(name);
        }

        @Override
        public <T> T getArgument(String name, Class<T> type) {
            return Field.from(arguments.get(name), type);
        }

        @Override
        public LocalDateTime getRequestedAt() {
            return timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ExecutionRequestImpl that)) return false;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }
}
