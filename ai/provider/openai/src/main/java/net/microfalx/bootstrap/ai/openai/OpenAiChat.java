package net.microfalx.bootstrap.ai.openai;

import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChat;

public class OpenAiChat extends AbstractChat {

    public OpenAiChat(Prompt prompt, Model model) {
        super(prompt, model);
    }
}
