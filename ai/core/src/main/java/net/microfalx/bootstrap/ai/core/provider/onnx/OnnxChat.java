package net.microfalx.bootstrap.ai.core.provider.onnx;

import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChat;

public class OnnxChat extends AbstractChat {

    public OnnxChat(Prompt prompt, Model model) {
        super(prompt, model);
    }
}
