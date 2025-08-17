package net.microfalx.bootstrap.ai.ollama;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChatFactory;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;

public class OllamaChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new AiNotFoundException("The model name is required for Ollama");
        }
        StreamingChatModel chatModel = OllamaStreamingChatModel.builder()
                .baseUrl(model.getUri().toASCIIString())
                .modelName(model.getModelName())
                .temperature(model.getTemperature())
                .stop(new ArrayList<>(model.getStopSequences()))
                .topP(model.getTopP()).topK(model.getTopK())
                .responseFormat(ResponseFormat.TEXT)
                .think(model.isThinking())
                .returnThinking(true)
                .timeout(getProperties().getChatRequestTimeout())
                .build();
        return new OllamaChat(prompt, model).setStreamingChatModel(chatModel);
    }
}
