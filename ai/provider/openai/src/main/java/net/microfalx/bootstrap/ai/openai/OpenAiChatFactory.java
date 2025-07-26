package net.microfalx.bootstrap.ai.openai;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChatFactory;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;

public class OpenAiChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new AiNotFoundException("The model name is required for OpenAI");
        }
        StreamingChatModel chatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(model.getUri().toASCIIString()).apiKey(model.getApyKey())
                .projectId(getProperties().getOpenAiProjectId())
                .organizationId(getProperties().getOpenAiOrganizationId())
                .modelName(model.getModelName())
                .temperature(model.getTemperature())
                .maxTokens(model.getMaximumOutputTokens())
                .frequencyPenalty(model.getFrequencyPenalty())
                .presencePenalty(model.getPresencePenalty())
                .maxCompletionTokens(model.getMaximumOutputTokens())
                .responseFormat(model.getResponseFormat().name())
                .stop(new ArrayList<>(model.getStopSequences())).strictTools(true)
                .topP(model.getTopP())
                .timeout(getProperties().getChatRequestTimeout())
                .build();
        return new OpenAiChat(prompt, model).setStreamingChatModel(chatModel);
    }
}
