package net.microfalx.bootstrap.ai.ollama;

import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChat;

public class OllamaChat extends AbstractChat {

    public OllamaChat(Prompt prompt, Model model) {
        super(prompt,model);
    }
}
