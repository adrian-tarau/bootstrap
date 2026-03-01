package net.microfalx.bootstrap.ai.core;

import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Tool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Builds the tools variable for the AI service.
 */
class ToolsBuilder {

    private static final String NO_TOOLS_AVAILABLE = "No tools available";


    private final AiServiceImpl service;
    private final Chat chat;
    private final Model model;

    public ToolsBuilder(AiServiceImpl service, Chat chat) {
        requireNonNull(service);
        requireNonNull(chat);
        this.service = service;
        this.chat = chat;
        this.model = chat.getModel();
    }

    /**
     * Builds the actual tools.
     *
     * @return a non-null instance
     */
    ToolCallback[] getTools() {
        if (!model.getTags().contains(Model.TOOLS_TAG)) return new ToolCallback[0];
        Map<String, ToolCallback> tools = new HashMap<>();
        for (Tool tool : service.getTools()) {
            ToolCallback toolCallback = AiTools.callbackFromTool(tool);
            if (toolCallback == null) toolCallback = new ToolCallbackImpl(tool, new ToolExecutor(service, chat, tool));
            tools.put(tool.getName(), toolCallback);
        }
        return tools.values().toArray(new ToolCallback[0]);
    }

    /**
     * Builds final prompt text based on available information.
     *
     * @return a non-null string
     */
    String getVariable() {
        if (!model.getTags().contains(Model.TOOLS_TAG)) return NO_TOOLS_AVAILABLE;
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (Tool tool : service.getTools()) {
            if (!chat.hasTool(tool.getName())) continue;
            builder.append(index).append(". **").append(tool.getName()).append("**: ")
                    .append(tool.getDescription()).append("\n");
            for (Tool.Parameter parameter : tool.getParameters().values()) {
                builder.append(" - **").append(parameter.getName()).append("** (")
                        .append(parameter.getType().name().toLowerCase()).append("): ")
                        .append(parameter.getDescription()).append("\n");
            }
            index++;
        }
        String variableDescription = builder.toString();
        return isNotEmpty(variableDescription) ? variableDescription : NO_TOOLS_AVAILABLE;
    }


    private static class ToolCallbackImpl implements ToolCallback {

        private final Tool tool;
        private final ToolExecutor toolExecutor;

        public ToolCallbackImpl(Tool tool, ToolExecutor toolExecutor) {
            this.tool = tool;
            this.toolExecutor = toolExecutor;
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return ToolDefinition.builder().name(tool.getName())
                    .description(tool.getDescription())
                    .inputSchema(AiTools.generateSchema(tool))
                    .build();
        }

        @Override
        public String call(String toolInput) {
            Tool.ExecutionResponse response = tool.getExecutor().execute(null);
            return "No data";
        }

    }


}
