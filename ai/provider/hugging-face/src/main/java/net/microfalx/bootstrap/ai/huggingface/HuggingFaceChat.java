package net.microfalx.bootstrap.ai.huggingface;

import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChat;

public class HuggingFaceChat extends AbstractChat {

    public HuggingFaceChat(Prompt prompt, Model model) {
        super(prompt, model);
    }
}
