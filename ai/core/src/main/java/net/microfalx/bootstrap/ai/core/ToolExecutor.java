package net.microfalx.bootstrap.ai.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiToolException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Tool;
import net.microfalx.bootstrap.model.Field;

import java.util.HashMap;
import java.util.Map;

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
        return tool.getExecutor().execute(new ExecutionRequestImpl(toolExecutionRequest.id(), arguments));
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

    private class ExecutionRequestImpl implements Tool.ExecutionRequest {

        private final String id;
        private final Map<String, Object> arguments;

        ExecutionRequestImpl(String id, Map<String, Object> arguments) {
            this.id = id;
            this.arguments = arguments;
        }

        @Override
        public String getId() {
            return id;
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
    }
}
