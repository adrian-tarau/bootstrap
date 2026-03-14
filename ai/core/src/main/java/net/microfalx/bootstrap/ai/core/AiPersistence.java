package net.microfalx.bootstrap.ai.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Provider;
import net.microfalx.bootstrap.ai.core.jpa.*;
import net.microfalx.bootstrap.jdbc.jpa.JpaPersistence;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.TextUtils;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class AiPersistence extends JpaPersistence implements InitializingBean {

    @Getter
    @Autowired
    private ProviderRepository providerRepository;

    @Getter
    @Autowired
    private ModelRepository modelRepository;

    @Getter
    @Autowired
    private PromptRepository promptRepository;

    @Getter
    @Autowired
    private ChatRepository chatRepository;

    AiServiceImpl aiService;

    @Override
    public void afterPropertiesSet() throws Exception {
        registerMapper(Provider.class, net.microfalx.bootstrap.ai.core.jpa.Provider.class);
        registerMapper(Model.class, net.microfalx.bootstrap.ai.core.jpa.Model.class, (s, d) -> {
            d.setProvider(execute(s.getProvider()));
        });
        registerMapper(net.microfalx.bootstrap.ai.api.Prompt.class, net.microfalx.bootstrap.ai.core.jpa.Prompt.class, (s, d) -> {
            if (s.getModel() != null) d.setModel(execute(s.getModel()));
        });
    }

    Chat findChat(String id) {
        return chatRepository.findById(id).orElse(null);
    }

    net.microfalx.bootstrap.ai.core.jpa.Provider execute(Provider provider) {
        NaturalIdEntityUpdater<net.microfalx.bootstrap.ai.core.jpa.Provider, Integer> updater = getUpdater(providerRepository);
        return updater.findByNaturalIdAndUpdateFrom(provider);
    }

    net.microfalx.bootstrap.ai.core.jpa.Model execute(Model model) {
        NaturalIdEntityUpdater<net.microfalx.bootstrap.ai.core.jpa.Model, Integer> updater = getUpdater(modelRepository);
        return updater.findByNaturalIdAndUpdateFrom(model);
    }

    Prompt execute(net.microfalx.bootstrap.ai.api.Prompt prompt) {
        NaturalIdEntityUpdater<Prompt, Integer> updater = getUpdater(promptRepository);
        return updater.findByNaturalIdAndUpdateFrom(prompt);
    }

    void execute(net.microfalx.bootstrap.ai.api.Chat chat) {
        if (chat.getMessageCount() == 0) return;
        Resource NA = Resource.text(StringUtils.NA_STRING);
        Resource promptsUri = NA;
        try {
            promptsUri = aiService.writeChatPrompt(chat);
        } catch (IOException e) {
            LOGGER.atError().setCause(e).log("Failed to extract chat prompt for {}", chat.getId());
        }
        Resource memoryUri = NA;
        try {
            memoryUri = aiService.writeChatMemory(chat);
        } catch (IOException e) {
            LOGGER.atError().setCause(e).log("Failed to extract chat memory for {}", chat.getId());
        }
        Resource logsUri = NA;
        try {
            logsUri = aiService.writeChatLogs(chat);
        } catch (IOException e) {
            LOGGER.atError().setCause(e).log("Failed to extract chat logs for {}", chat.getId());
        }
        Resource toolsUri = NA;
        try {
            toolsUri = aiService.writeChatTools(chat);
        } catch (IOException e) {
            LOGGER.atError().setCause(e).log("Failed to extract chat tools for {}", chat.getId());
        }
        Chat jpaChat = chatRepository.findById(chat.getId()).orElse(null);
        if (jpaChat == null) {
            jpaChat = new Chat();
            jpaChat.setId(chat.getId());
            jpaChat.setModel(execute(chat.getModel()));
            jpaChat.setPrompt(execute(chat.getPrompt()));
            jpaChat.setUser(chat.getUser().getName());
        }
        jpaChat.setName(TextUtils.abbreviateMiddle(chat.getName(), 100));
        jpaChat.setPromptUri(promptsUri.toURI().toASCIIString());
        jpaChat.setMemoryUri(memoryUri.toURI().toASCIIString());
        jpaChat.setLogsUri(logsUri.toURI().toASCIIString());
        jpaChat.setToolsUri(toolsUri.toURI().toASCIIString());
        jpaChat.setDuration(chat.getDuration());
        jpaChat.setFinishAt(chat.getFinishAt());
        jpaChat.setStartAt(chat.getStartAt());
        jpaChat.setTokenCount(chat.getTokenCount());
        jpaChat.setTimeToFirstToken(chat.getTimeToFirstToken());
        chatRepository.save(jpaChat);
    }

}
