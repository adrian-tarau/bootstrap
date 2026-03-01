package net.microfalx.bootstrap.ai.ollama;

import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChatFactory;
import net.microfalx.lang.StringUtils;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.ThinkOption;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;

public class OllamaChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new AiNotFoundException("The model name is required for Ollama");
        }
        OllamaChatOptions options = new OllamaChatOptions();
        options.setModel(model.getModelName());
        options.setThinkOption(new ThinkOption.ThinkBoolean(getProperties().isThinkingEnabled() && model.isThinking()));
        options.setMaxTokens(getProperties().getMaximumInputEvents());
        OllamaApi api = OllamaApi.builder()
                .baseUrl(model.getUri().toASCIIString())
                .build();
        ModelManagementOptions modelManagementOptions = ModelManagementOptions.builder()
                .pullModelStrategy(getPullModelStrategy())
                .maxRetries(getProperties().getModelPullRetryCount())
                .build();
        OllamaChatModel chatModel = OllamaChatModel.builder().ollamaApi(api)
                .defaultOptions(options)
                .modelManagementOptions(modelManagementOptions)
                .build();
        return new OllamaChat(prompt, model).setChatModel(chatModel);
    }

    private PullModelStrategy getPullModelStrategy() {
        return switch (getProperties().getModelPullStrategy()) {
            case ALWAYS -> PullModelStrategy.ALWAYS;
            case NEVER -> PullModelStrategy.NEVER;
            default -> PullModelStrategy.WHEN_MISSING;
        };
    }
}
