package net.microfalx.bootstrap.ai.core.provider.djl;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.llama.engine.LlamaEngine;
import ai.djl.llama.engine.LlamaTranslator;
import ai.djl.translate.TranslateException;
import net.microfalx.bootstrap.ai.api.AiException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

public class DjlChatModel implements ChatModel {

    private Model model;
    private LlamaTranslator<String, String> translator;

    @Override
    public ChatResponse call(Prompt prompt) {
        Engine engine = Engine.getEngine(LlamaEngine.ENGINE_NAME);
        Device cpu = Device.cpu();
        Model model = engine.newModel(prompt.getOptions().getModel(), cpu);
        LlamaTranslator<String, String> translator = new LlamaTranslator<>();
        List<Generation> generations = new ArrayList<>();
        try (Predictor<String, String> predictor = model.newPredictor(translator)) {
            try {
                String response = predictor.predict(prompt.getContents());
                generations.add(new Generation(AssistantMessage.builder().content(response).build()));
            } catch (TranslateException e) {
                throw new AiException("Failed to generate response", e);
            }
        }
        return ChatResponse.builder().generations(generations).build();
    }

    void init(net.microfalx.bootstrap.ai.api.Prompt prompt) {
        if (translator != null) return;
        Engine engine = Engine.getEngine(LlamaEngine.ENGINE_NAME);
        Device cpu = Device.cpu();
        model = engine.newModel(prompt.getModel().getModelName(), cpu);
        translator = new LlamaTranslator<>();
    }


}
