package net.microfalx.bootstrap.ai.core.provider.djl;

import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChat;

public class DjlChat extends AbstractChat {

    public DjlChat(Prompt prompt, Model model) {
        super(prompt, model);
    }
}
