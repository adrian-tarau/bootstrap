package net.microfalx.bootstrap.ai.llama;

import lombok.Setter;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChat;

import java.io.IOException;

public class LlamaChat extends AbstractChat {

    @Setter private LlamaServer server;

    public LlamaChat(Prompt prompt, Model model) {
        super(prompt, model);
    }

    @Override
    protected void ping() {
        server.ping();

    }

    @Override
    protected void doClose() throws IOException {
        server.stop();
    }
}
