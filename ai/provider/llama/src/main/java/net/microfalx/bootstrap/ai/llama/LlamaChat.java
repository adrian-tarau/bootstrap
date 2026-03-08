package net.microfalx.bootstrap.ai.llama;

import lombok.Setter;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChat;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LlamaChat extends AbstractChat {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LlamaChat.class);
    @Setter private LlamaServer server;

    public LlamaChat(Prompt prompt, Model model) {
        super(prompt, model);
    }

    @Override
    public void ping() {
        server.ping();
    }

    @Override
    protected void collectProcessLogs(Logger logger) {
        logger.info("#### Llama Server Properties ####");
        logger.info("-------");
        try {
            logger.append(Field.from(server.getProperties(), String.class));
        } catch (Exception e) {
            logger.error("Unable to load logs for server " + server.getId(), e);
        }
        logger.info("#### Llama Server Logs ####");
        logger.info("-------");
        try {
            logger.append(server.getLogs().loadAsString());
        } catch (IOException e) {
            logger.error("Unable to load logs for server " + server.getId(), e);
        }
        logger.info("-------");
    }

    @Override
    protected void doClose() throws IOException {
        server.stop();
    }
}
