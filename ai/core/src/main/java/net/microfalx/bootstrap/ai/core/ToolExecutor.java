package net.microfalx.bootstrap.ai.core;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Tool;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.ObjectUtils;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Slf4j
class ToolExecutor {

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
