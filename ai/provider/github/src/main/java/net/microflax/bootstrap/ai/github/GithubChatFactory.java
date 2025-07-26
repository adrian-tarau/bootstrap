package net.microflax.bootstrap.ai.github;

import com.azure.ai.inference.models.ChatCompletionsResponseFormatText;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChatFactory;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;


public class GithubChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new AiNotFoundException("The model name is required for Github");
        }
        StreamingChatModel chatModel = GitHubModelsStreamingChatModel.builder()
                .modelName(model.getModelName()).temperature(model.getTemperature())
                .frequencyPenalty(model.getFrequencyPenalty())
                .presencePenalty(model.getPresencePenalty()).topP(model.getTopP())
                .stop(new ArrayList<>(model.getStopSequences())).gitHubToken(model.getApyKey())
                .responseFormat(new ChatCompletionsResponseFormatText())
                .maxTokens(model.getMaximumOutputTokens())
                .build();
        return new GithubChat(prompt, model).setStreamingChatModel(chatModel);
    }
}
