package net.microfalx.bootstrap.ai.core;

import lombok.Getter;
import net.microfalx.bootstrap.jdbc.jpa.JpaPersistence;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.ai.api.AiException;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Provider;
import net.microfalx.bootstrap.ai.core.jpa.*;
import net.microfalx.lang.CollectionUtils;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static net.microfalx.lang.CollectionUtils.setToString;

@Component
class AiPersistence extends JpaPersistence {

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

    net.microfalx.bootstrap.ai.core.jpa.Provider execute(Provider provider) {
        NaturalIdEntityUpdater<net.microfalx.bootstrap.ai.core.jpa.Provider, Integer> updater = getUpdater(providerRepository);
        net.microfalx.bootstrap.ai.core.jpa.Provider jpaProvider = new net.microfalx.bootstrap.ai.core.jpa.Provider();
        jpaProvider.setLicense(provider.getLicense());
        jpaProvider.setAuthor(provider.getAuthor());
        jpaProvider.setNaturalId(provider.getId());
        jpaProvider.setVersion(provider.getVersion());
        if (provider.getUri() == null) {
            jpaProvider.setUri(null);
        } else {
            jpaProvider.setUri(provider.getUri().toASCIIString());
        }
        jpaProvider.setTags(setToString(provider.getTags()));
        jpaProvider.setName(provider.getName());
        jpaProvider.setDescription(provider.getDescription());
        jpaProvider.setApiKey(provider.getApyKey());
        jpaProvider.setTags(CollectionUtils.setToString(provider.getTags()));
        return updater.findByNaturalIdAndUpdate(jpaProvider);
    }

    net.microfalx.bootstrap.ai.core.jpa.Model execute(Model model) {
        NaturalIdEntityUpdater<net.microfalx.bootstrap.ai.core.jpa.Model, Integer> updater = getUpdater(modelRepository);
        net.microfalx.bootstrap.ai.core.jpa.Model jpaModel = new net.microfalx.bootstrap.ai.core.jpa.Model();
        jpaModel.setNaturalId(model.getId());
        jpaModel.setModelName(model.getModelName());
        jpaModel.setApiKey(model.getApyKey(false));
        jpaModel.setFrequencyPenalty(model.getFrequencyPenalty());
        jpaModel.setPresencePenalty(model.getPresencePenalty());
        jpaModel.setMaximumOutputTokens(model.getMaximumOutputTokens());
        if (jpaModel.getUri() == null) {
            jpaModel.setUri(null);
        } else {
            jpaModel.setUri(model.getUri(false).toASCIIString());
        }
        jpaModel.setTemperature(model.getTemperature());
        jpaModel.setTopK(model.getTopK());
        jpaModel.setTopP(model.getTopP());
        jpaModel.setResponseFormat(model.getResponseFormat());
        jpaModel.setStopSequences(setToString(model.getStopSequences()));
        jpaModel.setName(model.getName());
        jpaModel.setTags(setToString(model.getTags()));
        jpaModel.setDescription(model.getDescription());
        jpaModel.setName(model.getName());
        jpaModel.setDefault(model.isDefault());
        jpaModel.setEnabled(model.isEnabled());
        jpaModel.setEmbedding(model.isEmbedding());
        jpaModel.setProvider(execute(model.getProvider()));
        jpaModel.setDescription(model.getDescription());
        jpaModel.setTags(CollectionUtils.setToString(model.getTags()));
        jpaModel.setMaximumContextLength(model.getMaximumContextLength());
        jpaModel.setThinking(model.isThinking());
        return updater.findByNaturalIdAndUpdate(jpaModel);
    }

    void execute(net.microfalx.bootstrap.ai.api.Chat chat) {
        if (chat.getMessageCount() == 0) return;
        Resource resource;
        try {
            resource = aiService.writeChatMessages(chat);
        } catch (IOException e) {
            throw new AiException("Failed to write chat messages for " + chat.getId(), e);
        }
        Chat jpaChat = chatRepository.findById(chat.getId()).orElse(null);
        if (jpaChat == null) {
            jpaChat = new Chat();
            jpaChat.setId(chat.getId());
            jpaChat.setModel(execute(chat.getModel()));
            jpaChat.setUser(chat.getUser().getName());
        }
        jpaChat.setName(chat.getName());
        jpaChat.setResource(resource.toURI().toASCIIString());
        jpaChat.setDuration(chat.getDuration());
        jpaChat.setFinishAt(chat.getFinishAt());
        jpaChat.setStartAt(chat.getStartAt());
        jpaChat.setTokenCount(chat.getTokenCount());
        chatRepository.save(jpaChat);
    }

    void execute(net.microfalx.bootstrap.ai.api.Prompt prompt) {
        NaturalIdEntityUpdater<Prompt, Integer> updater = getUpdater(promptRepository);
        Prompt jpaPrompt = new Prompt();
        jpaPrompt.setNaturalId(prompt.getId());
        jpaPrompt.setName(prompt.getName());
        jpaPrompt.setRole(prompt.getRole());
        jpaPrompt.setMaximumInputEvents(prompt.getMaximumInputEvents());
        jpaPrompt.setMaximumOutputTokens(prompt.getMaximumOutputTokens());
        jpaPrompt.setChainOfThought(prompt.isChainOfThought());
        jpaPrompt.setUseOnlyContext(prompt.isUseOnlyContext());
        jpaPrompt.setExamples(prompt.getExamples());
        jpaPrompt.setContext(prompt.getContext());
        jpaPrompt.setQuestion(prompt.getQuestion());
        jpaPrompt.setTags(setToString(prompt.getTags()));
        jpaPrompt.setSystem(prompt.isSystem());
        jpaPrompt.setDescription(prompt.getDescription());
        jpaPrompt.setSystem(prompt.isSystem());
        jpaPrompt.setInstructions(prompt.getInstructions());
        if (prompt.getModel() != null) jpaPrompt.setModel(execute(prompt.getModel()));
        updater.findByNaturalIdAndUpdate(jpaPrompt);
    }

}
