package net.microfalx.bootstrap.ai.huggingface;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChatFactory;

import static net.microfalx.lang.StringUtils.isEmpty;

public class HuggingFaceChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (isEmpty(model.getModelName())) {
            throw new AiNotFoundException("The model name is required for HuggingFace");
        }
        ChatModel chatModel = HuggingFaceChatModel.builder()
                .accessToken(model.getApyKey())
                .baseUrl(model.getUri().toASCIIString())
                .returnFullText(true)
                .modelId(model.getId())
                .temperature(model.getTemperature())
                .maxNewTokens(model.getMaximumOutputTokens())
                .build();
        return new HuggingFaceChat(prompt, model).setChatModel(chatModel);
    }
}
