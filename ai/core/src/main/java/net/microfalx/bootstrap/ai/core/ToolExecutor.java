package net.microfalx.bootstrap.ai.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiToolException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Tool;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.bootstrap.ai.core.AiUtils.TOOL_EXECUTION_METRICS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Slf4j
class ToolExecutor implements dev.langchain4j.service.tool.ToolExecutor {

    private final AiServiceImpl service;
    private final Chat chat;
    private final Model model;
    private final Tool tool;

    ToolExecutor(AiServiceImpl service, Chat chat, Tool tool) {
        requireNonNull(tool);
        requireNonNull(chat);
        requireNonNull(tool);
        this.service = service;
        this.chat = chat;
        this.tool = tool;
        this.model = chat.getModel();
    }

    private String doExecute(ToolExecutionRequest toolExecutionRequest, Object memoryId) {
        Map<String, Object> arguments = getArguments(toolExecutionRequest);
        ExecutionRequestImpl executionRequest = new ExecutionRequestImpl(arguments);
        Tool.ExecutionResponse executionResponse = tool.getExecutor().execute(executionRequest);
        if (chat instanceof AbstractChat abstractChat) {
            abstractChat.registerToolExecution(executionRequest, executionResponse);
        }
        try {
            return executionResponse.getContent().getResource().loadAsString();
        } catch (IOException e) {
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        }
    }

    @Override
    public String execute(ToolExecutionRequest toolExecutionRequest, Object memoryId) {
        LOGGER.info("Executing tool: '{}', arguments '{}'", tool.getName(), toolExecutionRequest.arguments());
        return TOOL_EXECUTION_METRICS.time(tool.getName(), () -> doExecute(toolExecutionRequest, memoryId));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getArguments(ToolExecutionRequest toolExecutionRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> decodedValues = mapper.readValue(toolExecutionRequest.arguments(), Map.class);
            return new HashMap<>(decodedValues);
        } catch (JsonProcessingException e) {
            throw new AiToolException("Failed to decode tool arguments for tool '" + tool.getName() + "'", e);
        }
    }

    @ToString
    private class ExecutionRequestImpl implements Tool.ExecutionRequest {

        private final String id = UUID.randomUUID().toString();
        private final Map<String, Object> arguments;

        ExecutionRequestImpl(Map<String, Object> arguments) {
            this.arguments = arguments;
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
