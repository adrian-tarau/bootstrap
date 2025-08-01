package net.microfalx.bootstrap.ai.core;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import net.microfalx.bootstrap.dataset.DataSetRequest;
import net.microfalx.bootstrap.security.SecurityContext;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Message;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.FormatterUtils.formatNumber;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Base class for chat sessions.
 */
public abstract class AbstractChat extends NamedAndTaggedIdentifyAware<String> implements Chat {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractChat.class);

    private static final String DEFAULT_NAME = "Unnamed Chat";

    private final Model model;
    private final Prompt prompt;
    private final LocalDateTime startAt = LocalDateTime.now();
    private LocalDateTime finishAt;
    private ChatModel chatModel;
    private StreamingChatModel streamingChatModel;

    private ChatMemory chatMemory;
    private SimpleChat chat;
    private StreamChat streamChat;
    private AiServiceImpl service;

    private boolean disableTools;
    private Set<String> disabledTools = new CopyOnWriteArraySet<>();

    private volatile Principal principal;
    private volatile String systemMessage;

    final AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());
    private final AtomicInteger inputTokenCount = new AtomicInteger();
    private final AtomicInteger outputTokenCount = new AtomicInteger();
    private final Set<Object> features = new CopyOnWriteArraySet<>();
    final AtomicBoolean changed = new AtomicBoolean();
    final AtomicBoolean internal = new AtomicBoolean(false);

    private static final Map<String, AtomicInteger> CHAT_COUNTERS = new ConcurrentHashMap<>();

    public AbstractChat(Prompt prompt, Model model) {
        requireNonNull(prompt);
        requireNonNull(model);
        setId(UUID.randomUUID().toString());
        setName(DEFAULT_NAME);
        this.prompt = prompt;
        this.model = model;
        updateTags(prompt.getTags());
        updateTags(model.getTags());
    }

    @Override
    public final Model getModel() {
        return model;
    }

    @Override
    public final Prompt getPrompt() {
        return prompt;
    }

    @Override
    public final Principal getUser() {
        return principal;
    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public LocalDateTime getStartAt() {
        return startAt;
    }

    @Override
    public LocalDateTime getFinishAt() {
        return finishAt;
    }

    @Override
    public int getTokenCount() {
        return inputTokenCount.get() + outputTokenCount.get();
    }

    @Override
    public Duration getDuration() {
        return Duration.between(startAt, finishAt != null ? finishAt : LocalDateTime.now());
    }

    @Override
    public Message getSystemMessage() {
        return getMessages().stream().filter(message -> message.getType() == Message.Type.SYSTEM)
                .findFirst().orElse(MessageImpl.create(Message.Type.SYSTEM, "System message is not available"));
    }

    @Override
    public Collection<Message> getMessages() {
        return chatMemory.messages().stream()
                .map(MessageImpl::create)
                .collect(Collectors.toList());
    }

    @Override
    public int getMessageCount() {
        return chatMemory.messages().size();
    }

    @Override
    public String ask(String message) {
        validate();
        if (chatModel != null) {
            return chatModel.chat(message);
        } else {
            StringBuilder builder = new StringBuilder();
            Iterator<String> stream = chat(message);
            while (stream.hasNext()) {
                String token = stream.next();
                builder.append(token);
            }
            return builder.toString();
        }
    }

    @Override
    public net.microfalx.bootstrap.ai.api.TokenStream chat(String message) {
        validate();
        if (streamingChatModel != null) {
            TokenStream stream = streamChat.chat(message);
            TokenStreamHandler handler = new TokenStreamHandler(service, this, stream);
            service.getChatPool().execute(stream::start);
            return handler;
        } else {
            String answer = ask(message);
            String[] parts = StringUtils.split(answer, " ");
            return new TokenStreamImpl(Arrays.asList(parts).iterator());
        }
    }

    @Override
    public <F> void addFeature(F feature) {
        if (feature != null) this.features.add(feature);
    }

    @Override
    public <F> F getFeature(Class<F> featureType) {
        requireNonNull(featureType);
        for (Object feature : features) {
            if (featureType.isInstance(feature)) {
                return featureType.cast(feature);
            }
        }
        return null;
    }

    @Override
    public boolean hasTool(String name) {
        requireNotEmpty(name);
        if (disableTools) {
            return false;
        } else {
            return disabledTools.contains(name.toLowerCase());
        }
    }

    @Override
    public void disableTool(String name) {
        requireNotEmpty(name);
        disabledTools.add(name.toLowerCase());
    }

    @Override
    public void disableTools() {
        this.disableTools = true;
    }

    public void updateName(String name) {
        if (StringUtils.isNotEmpty(name)) setName(name);
    }

    public final AbstractChat setChatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
        return this;
    }

    public final AbstractChat setStreamingChatModel(StreamingChatModel streamingChatModel) {
        requireNonNull(streamingChatModel);
        this.streamingChatModel = streamingChatModel;
        return this;
    }

    protected void doClose() throws IOException {
        // default implementation does nothing
    }

    private void validate() {
        if (chatModel == null && streamingChatModel == null) {
            throw new IllegalStateException("No chat model has been set");
        }
        updateLastActivity();
    }

    @Override
    public final void close() {
        finishAt = LocalDateTime.now();
        try {
            doClose();
        } catch (IOException e) {
            LOGGER.atWarn().setCause(e).log("Failed to close chat session {}", getNameAndId());
        }
        if (service != null && !internal.get()) service.closeChat(this);
    }

    void initialize(AiServiceImpl service) {
        requireNonNull(service);
        validate();
        initializePrincipal();
        this.service = service;
        TokenCountEstimator tokenCountEstimator = new CustomHuggingFaceTokenCountEstimator();
        this.chatMemory = TokenWindowChatMemory.builder().id(getId())
                .maxTokens(model.getMaximumContextLength(), tokenCountEstimator)
                .chatMemoryStore(service.getChatStore())
                .build();
        if (chatModel != null) {
            chat = updateAiService(AiServices.builder(SimpleChat.class)).build();
        } else {
            streamChat = updateAiService(AiServices.builder(StreamChat.class)).build();
        }
        streamCompleted(new TokenStreamImpl(Collections.emptyIterator()));
        if (isNotEmpty(prompt.getQuestion())) summarize(prompt.getQuestion());
    }

    void streamCompleted(net.microfalx.bootstrap.ai.api.TokenStream tokenStream) {
        inputTokenCount.addAndGet(tokenStream.getInputTokenCount());
        outputTokenCount.addAndGet(tokenStream.getOutputTokenCount());
        updateDescription();
        changed.set(true);
    }

    private void initializePrincipal() {
        principal = SecurityContext.get().getPrincipal();
        setName(DEFAULT_NAME + String.format(" %03d", getNextChatIndex()));
    }

    private void summarize(String text) {
        service.getChatPool().execute(() -> {
            String summarize = service.summarize(text, true);
            updateName(summarize);
        });
    }

    private <T> AiServices<T> updateAiService(AiServices<T> aiService) {
        aiService.chatMemory(chatMemory)
                .systemMessageProvider(new SystemMessageProvider())
                .contentRetriever(service.getContentRetriever())
                .toolProvider(new ToolProviderImpl())
                .maxSequentialToolsInvocations(1);
        if (streamingChatModel != null) {
            aiService.streamingChatModel(streamingChatModel);
        } else if (chatModel != null) {
            aiService.chatModel(chatModel);
        } else {
            throw new IllegalStateException("No chat model has been set");
        }
        return aiService;
    }

    private void updateDescription() {
        StringBuilder builder = new StringBuilder();
        addDefinitionList(builder, "Model", model.getName() + " (" + model.getProvider().getName()
                + "), content length: " + model.getMaximumContextLength());
        addDefinitionList(builder, "Tokens", "_Input_: " + inputTokenCount.get()
                + ", _Output_: " + outputTokenCount.get() + ", _Total_: " + (inputTokenCount.get() + outputTokenCount.get()));
        addDefinitionList(builder, "Parameters", "_Temperature_: " + formatNumber(model.getTemperature())
                + ", _TopP_: " + model.getTopP() + ", _TopK_: " + model.getTopK());
        DataSetRequest<?, ?, ?> dataSetRequest = getFeature(DataSetRequest.class);
        if (dataSetRequest != null) {
            addDefinitionList(builder, "Data Set",
                    "_Name_:" + dataSetRequest.getDataSet().getName()
                            + ", _Filter_: " + dataSetRequest.getFilter().getDescription());
        }
        Page<?> page = getFeature(Page.class);
        if (page != null) {
            addDefinitionList(builder, "Data", "_Event Count_:" + page.getSize()
                    + ", _Total Event Count_: " + page.getTotalElements());
        }
        setDescription(builder.toString());
    }

    private void addDefinitionList(StringBuilder builder, String term, String... descriptions) {
        builder.append(term).append("\n");
        for (String description : descriptions) {
            builder.append(": ").append(description).append("\n");
        }
        builder.append("\n");
    }

    private void updateLastActivity() {
        lastActivity.set(System.currentTimeMillis());
    }

    private int getNextChatIndex() {
        AtomicInteger counter = CHAT_COUNTERS.computeIfAbsent(getUser().getName(), id -> new AtomicInteger(1));
        return counter.getAndIncrement();
    }

    public interface SimpleChat {

        Result<String> chat(String message);
    }

    public interface StreamChat {

        TokenStream chat(String message);
    }

    private class SystemMessageProvider implements Function<Object, String> {

        @Override
        public String apply(Object o) {
            if (systemMessage == null) {
                systemMessage = service.getSystemMessage(AbstractChat.this);
                updateDescription();
            }
            return systemMessage;
        }
    }

    private class ToolProviderImpl implements ToolProvider {

        @Override
        public ToolProviderResult provideTools(ToolProviderRequest request) {
            Map<ToolSpecification, ToolExecutor> tools = new HashMap<>();
            if (!disableTools) tools = new ToolsBuilder(service, AbstractChat.this).getTools();
            return new ToolProviderResult(tools);
        }
    }

    private static class CustomHuggingFaceTokenCountEstimator extends HuggingFaceTokenCountEstimator {

        @Override
        public int estimateTokenCountInText(String text) {
            if (StringUtils.isEmpty(text)) return 0;
            return super.estimateTokenCountInText(text);
        }
    }
}
