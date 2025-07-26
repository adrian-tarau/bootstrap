package net.microfalx.bootstrap.ai.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.api.Provider;
import net.microfalx.bootstrap.ai.core.jpa.ModelRepository;
import net.microfalx.bootstrap.ai.core.jpa.PromptRepository;
import net.microfalx.lang.CollectionUtils;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.setFromString;
import static net.microfalx.lang.ObjectUtils.defaultIfNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.toIdentifier;
import static net.microfalx.lang.UriUtils.parseUri;

public class AiCache extends ApplicationContextSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiCache.class);

    private final AiServiceImpl aiService;
    private volatile AiCache oldCache;

    private final Map<String, net.microfalx.bootstrap.ai.api.Model> models = new HashMap<>();
    private final Map<String, Provider> providers = new HashMap<>();
    private final Map<String, Prompt> prompts = new HashMap<>();

    AiCache(AiServiceImpl aiService, AiCache oldCache) {
        requireNonNull(aiService);
        this.aiService = aiService;
        this.oldCache = oldCache;
    }

    Map<String, Model> getModels() {
        return models;
    }

    void registerModel(net.microfalx.bootstrap.ai.api.Model model) {
        models.put(toIdentifier(model.getId()), model);
    }

    Model findModel(String id) {
        requireNonNull(id);
        return models.get(toIdentifier(id));
    }

    Model getModel(String id) {
        Model model = findModel(id);
        if (model == null) {
            throw new AiNotFoundException("A model with identifier '" + id + "' is not registered");
        }
        return model;
    }

    void registerProvider(net.microfalx.bootstrap.ai.api.Provider provider) {
        providers.put(toIdentifier(provider.getId()), provider);
        for (net.microfalx.bootstrap.ai.api.Model model : provider.getModels()) {
            registerModel(model);
        }
    }

    Map<String, Provider> getProviders() {
        return providers;
    }

    Provider getProvider(String id) {
        requireNonNull(id);
        Provider cluster = providers.get(toIdentifier(id));
        if (cluster == null) {
            throw new AiNotFoundException("A provider with identifier '" + id + "' is not registered");
        }
        return cluster;
    }

    Map<String, Prompt> getPrompts() {
        return prompts;
    }

    void registerPrompt(Prompt prompt) {
        prompts.put(toIdentifier(prompt.getId()), prompt);
    }

    Prompt getPrompt(String id) {
        requireNonNull(id);
        Prompt prompt = prompts.get(toIdentifier(id));
        if (prompt == null) {
            throw new AiNotFoundException("A prompt with identifier '" + id + "' is not registered");
        }
        return prompt;
    }

    void load() {
        LOGGER.info("Load models and providers");
        try {
            loadModels();
        } catch (Exception e) {
            LOGGER.error("Failed to load models", e);
        } finally {
            oldCache = null;
        }
        try {
            loadPrompts();
        } catch (Exception e) {
            LOGGER.error("Failed to load prompts", e);
        } finally {
            oldCache = null;
        }
        LOGGER.info("Loaded completed, providers: {}, models: {}, prompts: {}", providers.size(), models.size(), prompts.size());
    }

    private void loadModels() {
        AiProperties properties = aiService.getProperties();
        Map<Integer, Provider.Builder> providerBuilders = new HashMap<>();
        List<net.microfalx.bootstrap.ai.core.jpa.Model> modelJpas = getBean(ModelRepository.class).findAll();
        for (net.microfalx.bootstrap.ai.core.jpa.Model modelJpa : modelJpas) {
            Model.Builder modelBuild = loadModel(modelJpa, properties);
            Provider.Builder providerBuild = providerBuilders.get(modelJpa.getProvider().getId());
            if (providerBuild == null) {
                providerBuild = loadProvider(modelJpa.getProvider());
                if (providerBuild != null) {
                    providerBuilders.put(modelJpa.getProvider().getId(), providerBuild);
                }
            }
            if (providerBuild != null) {
                providerBuild.model(modelBuild);
            }
        }
        providerBuilders.values().forEach(builder -> registerProvider(builder.build()));
    }

    private Model.Builder loadModel(net.microfalx.bootstrap.ai.core.jpa.Model modelJpa, AiProperties properties) {
        String modelId = modelJpa.getNaturalId();
        modelId = StringUtils.replaceFirst(modelId, modelJpa.getProvider().getNaturalId() + "_", EMPTY_STRING);
        Model.Builder builder = new Model.Builder(modelId)
                .addStopSequences(new ArrayList<>(CollectionUtils.setFromString(modelJpa.getStopSequences()))).frequencyPenalty(modelJpa.getFrequencyPenalty())
                .modelName(modelJpa.getModelName()).maximumOutputTokens(modelJpa.getMaximumOutputTokens())
                .presencePenalty(modelJpa.getPresencePenalty())
                .temperature(defaultIfNull(modelJpa.getTemperature(), properties.getDefaultTemperature()));
        builder.uri(parseUri(modelJpa.getUri()))
                .apyKey(modelJpa.getApiKey());
        builder.topK(defaultIfNull(modelJpa.getTopK(), properties.getDefaultTopK()))
                .topP(defaultIfNull(modelJpa.getTopP(), properties.getDefaultTopP()))
                .responseFormat(modelJpa.getResponseFormat()).setDefault(modelJpa.isDefault())
                .maximumContextLength(modelJpa.getMaximumContextLength())
                .enabled(modelJpa.isEnabled()).embedding(modelJpa.isEmbedding()).thinking(modelJpa.isThinking());
        builder.tags(setFromString(modelJpa.getTags())).name(modelJpa.getName())
                .description(modelJpa.getDescription());
        return builder;
    }

    private Provider.Builder loadProvider(net.microfalx.bootstrap.ai.core.jpa.Provider providerJpa) {
        Provider oldProvider = null;
        try {
            if (oldCache != null) oldProvider = oldCache.getProvider(providerJpa.getNaturalId());
        } catch (AiNotFoundException e) {
            return null;
        }
        if (oldProvider == null) {
            LOGGER.warn("No previous provider found for '{}', skipping", providerJpa.getNaturalId());
            return null;
        }
        Provider.Builder builder = new Provider.Builder(providerJpa.getNaturalId())
                .author(providerJpa.getAuthor());
        builder.uri(parseUri(providerJpa.getUri()))
                .apyKey(providerJpa.getApiKey())
                .license(providerJpa.getLicense()).version(providerJpa.getVersion());
        builder.tags(CollectionUtils.setFromString(providerJpa.getTags()))
                .name(providerJpa.getName()).description(providerJpa.getDescription());
        builder.chatFactory(oldProvider.getChatFactory());
        try {
            builder.embeddingFactory(oldProvider.getEmbeddingFactory());
        } catch (IllegalStateException e) {
            // ignore if the embedding factory is not available
        }
        return builder;
    }

    private void loadPrompts() {
        List<net.microfalx.bootstrap.ai.core.jpa.Prompt> promptJpas = getBean(PromptRepository.class).findAll();
        promptJpas.forEach(promptJpa -> {
            registerPrompt(loadPrompt(promptJpa));
        });
    }

    private Prompt loadPrompt(net.microfalx.bootstrap.ai.core.jpa.Prompt promptJpa) {
        Prompt.Builder builder = new Prompt.Builder()
                .chainOfThought(promptJpa.isChainOfThought())
                .context(promptJpa.getContext())
                .examples(promptJpa.getExamples())
                .maximumInputEvents(promptJpa.getMaximumInputEvents())
                .maximumOutputTokens(promptJpa.getMaximumOutputTokens())
                .question(promptJpa.getQuestion())
                .role(promptJpa.getRole()).system(promptJpa.isSystem())
                .instructions(promptJpa.getInstructions())
                .thinking(promptJpa.isThinking());
        if (promptJpa.getModel() != null) {
            builder.model(getModel(promptJpa.getModel().getNaturalId()));
        }
        builder.useOnlyContext(promptJpa.isUseOnlyContext())
                .tags(CollectionUtils.setFromString(promptJpa.getTags()))
                .name(promptJpa.getName()).description(promptJpa.getDescription())
                .id(promptJpa.getNaturalId());
        return builder.build();
    }
}
