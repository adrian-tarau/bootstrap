package net.microfalx.bootstrap.ai.core;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Tool;

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
        return "NOT AVAILABLE";
    }

    @Override
    public String execute(ToolExecutionRequest toolExecutionRequest, Object memoryId) {
        LOGGER.info("Executing tool: '{}', arguments '{}'", tool.getName(), toolExecutionRequest.arguments());
        return TOOL_EXECUTION_METRICS.time(tool.getName(), () -> doExecute(toolExecutionRequest, memoryId));
    }
}
