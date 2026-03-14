package net.microfalx.bootstrap.ai.llama;

import lombok.Setter;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChat;
import net.microfalx.bootstrap.model.Types;
import net.microfalx.lang.Logger;

import java.io.IOException;

import static net.microfalx.bootstrap.help.HelpUtilities.TEXT_BLOCK_END;
import static net.microfalx.bootstrap.help.HelpUtilities.TEXT_BLOCK_START;

public class LlamaChat extends AbstractChat {

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
        logger.info("#### Llama Server Properties");
        logger.ln().append(TEXT_BLOCK_START);
        try {
            logger.append(Types.asString(server.getProperties()));
        } catch (Exception e) {
            logger.error("Unable to load logs for server " + server.getId(), e);
        }
        logger.info(TEXT_BLOCK_END);
        logger.info("#### Llama Server Logs");
        logger.ln().append(TEXT_BLOCK_START);
        try {
            logger.append(server.getLogs().loadAsString());
        } catch (IOException e) {
            logger.error("Unable to load logs for server " + server.getId(), e);
        }
        logger.info(TEXT_BLOCK_END);
    }

    @Override
    protected void doClose() throws IOException {
        server.stop();
    }
}
