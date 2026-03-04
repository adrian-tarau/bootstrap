package net.microfalx.bootstrap.ai.llama;

import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChatFactory;
import net.microfalx.lang.StringUtils;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

public class LlamaChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new AiNotFoundException("The model name is required for OpenAI");
        }
        LlamaChat chat = new LlamaChat(prompt, model);
        LlamaServer server = LlamaServerFactory.getInstance().start(chat);
        chat.setServer(server);
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(server.getUri(true).toASCIIString()).apiKey("dummy")
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model.getModelName())
                .build();
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
        chat.setChatModel(chatModel);
        return chat;
    }
}
