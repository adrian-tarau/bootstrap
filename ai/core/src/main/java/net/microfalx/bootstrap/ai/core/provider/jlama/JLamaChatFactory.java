package net.microfalx.bootstrap.ai.core.provider.jlama;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.jlama.JlamaStreamingChatModel;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChatFactory;
import net.microfalx.lang.NumberUtils;
import net.microfalx.lang.StringUtils;

public class JLamaChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new AiNotFoundException("The model name is required for JLama");
        }
        StreamingChatModel chatModel = JlamaStreamingChatModel.builder()
                .modelName(model.getModelName()).temperature(NumberUtils.toFloat(model.getTemperature()))
                .maxTokens(model.getMaximumOutputTokens())
                .modelCachePath(getModelCacheDirectory("jlama").toPath())
                .workingDirectory(getWorkingDirectory("jlama").toPath())
                .build();
        return new JLamaChat(prompt, model).setStreamingChatModel(chatModel);
    }

}
