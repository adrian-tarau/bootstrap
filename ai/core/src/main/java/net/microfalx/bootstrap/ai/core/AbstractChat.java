package net.microfalx.bootstrap.ai.core;

import net.microfalx.bootstrap.ai.api.*;
import net.microfalx.bootstrap.dataset.DataSetRequest;
import net.microfalx.bootstrap.security.SecurityContext;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;

import java.io.File;
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
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.FormatterUtils.formatBytes;
import static net.microfalx.lang.FormatterUtils.formatNumber;
import static net.microfalx.lang.StringUtils.isEmpty;
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

    private ChatMemory chatMemory;
    private ChatClient client;
    private TokenCountEstimator tokenCountEstimator;
    private AiServiceImpl service;

    private boolean disableTools;
    private final Set<String> disabledTools = new CopyOnWriteArraySet<>();
    private final Set<Tool> tools = new CopyOnWriteArraySet<>();
    private final Map<String, Object> variables = new ConcurrentHashMap<>();
    private final net.microfalx.lang.Logger logger = net.microfalx.lang.Logger.create();

    private volatile Principal principal;
    private volatile String systemMessage;
    private volatile String userMessage;

    final AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());
    private final AtomicInteger inputTokenCount = new AtomicInteger();
    private final AtomicInteger outputTokenCount = new AtomicInteger();
    private final AtomicLong timeToFirstTokenTotal = new AtomicLong();
    private final AtomicInteger timeToFirstTokenCount = new AtomicInteger();
    private final AtomicBoolean nameChanged = new AtomicBoolean();
    private final AtomicBoolean nameChangePending = new AtomicBoolean();
    private final Set<Object> features = new CopyOnWriteArraySet<>();
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<Tool.ExecutionRequest, Tool.ExecutionResponse> toolExecutions = new ConcurrentHashMap<>();
    private final Map<Tool.ExecutionRequest, Throwable> toolExecutionFailures = new ConcurrentHashMap<>();
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
    public int getTokenCount(String text) {
        if (StringUtils.isEmpty(text)) return 0;
        return tokenCountEstimator.estimate(text);
    }

    @Override
    public Duration getTimeToFirstToken() {
        return timeToFirstTokenCount.get() == 0 ? Duration.ZERO : Duration.ofMillis(timeToFirstTokenTotal.get() / timeToFirstTokenCount.get());
    }

    @Override
    public Duration getDuration() {
        return Duration.between(startAt, finishAt != null ? finishAt : LocalDateTime.now());
    }

    @Override
    public Message getSystemMessage() {
        if (isEmpty(systemMessage)) systemMessage = doGetSystemMessage();
        return MessageImpl.create(Message.Type.SYSTEM, systemMessage != null ? systemMessage : StringUtils.EMPTY_STRING);
    }

    @Override
    public Collection<Message> getMessages(boolean includeSystemMessage) {
        Collection<Message> messages = new ArrayList<>();
        Message currentSystemMessage = getSystemMessage();
        if (includeSystemMessage && !currentSystemMessage.isEmpty()) messages.add(currentSystemMessage);
        chatMemory.get(getId()).stream().map(MessageImpl::create).forEach((messages::add));
        return messages;
    }

    @Override
    public Collection<Message> getMessages() {
        return getMessages(false);
    }

    @Override
    public int getMessageCount() {
        return chatMemory.get(getId()).size();
    }

    @Override
    public String ask(String message) {
        validate();
        StringBuilder builder = new StringBuilder();
        net.microfalx.bootstrap.ai.api.TokenStream stream = chat(message);
        while (stream.hasNext()) {
            Token token = stream.next();
            builder.append(token.getText());
        }
        return builder.toString();
    }

    @Override
    public net.microfalx.bootstrap.ai.api.TokenStream chat(String message) {
        validate();
        Flux<ChatResponse> chatResponse = client.prompt(message).stream().chatResponse();
        return new TokenStreamHandler(service, this, chatResponse);
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
    public Collection<Tool> getTools() {
        Collection<Tool> allTools = new ArrayList<>(service.getTools());
        allTools.addAll(tools);
        return allTools.stream().filter(tool -> !hasTool(tool.getName())).toList();
    }

    @Override
    public Map<Tool.ExecutionRequest, Tool.ExecutionResponse> getToolExecutions() {
        return unmodifiableMap(toolExecutions);
    }

    @Override
    public Map<Tool.ExecutionRequest, Throwable> getToolExecutionFailures() {
        return unmodifiableMap(toolExecutionFailures);
    }

    @Override
    public String getToolsDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("#### Tools\n\n");
        for (Tool tool : getTools()) {
            sb.append(tool.getName()).append("\n").append(": ").append(tool.getDescription()).append("\n\n");
        }
        if (!toolExecutions.isEmpty()) {
            sb.append("""
                    #### Requests
                    
                    | Invocation | Items | Tokens | Size |
                    | ---------- | ----- | ------ | ---- |                    
                    """);
            for (Map.Entry<Tool.ExecutionRequest, Tool.ExecutionResponse> entry : toolExecutions.entrySet()) {
                Tool.ExecutionRequest request = entry.getKey();
                Tool.ExecutionResponse response = entry.getValue();
                sb.append("|").append(request.getDescription())
                        .append("|").append(response.getItemCount())
                        .append("|").append(response.getTokenCount())
                        .append("|").append(formatBytes(response.getContent().getSize()))
                        .append("|\n");
            }
        }
        return sb.toString();
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
    public void addTool(Tool tool) {
        requireNonNull(tool);
        this.tools.add(tool);
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

    @Override
    public Map<String, Object> getVariables() {
        return unmodifiableMap(variables);
    }

    @Override
    public void addVariable(String name, Object value) {
        requireNonNull(name);
        variables.put(name.toLowerCase(), value);
    }

    @Override
    public void addAttribute(String name, Object value) {
        requireNotEmpty(name);
        attributes.put(name.toLowerCase(), value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(String name) {
        requireNotEmpty(name);
        return (T) attributes.get(name.toLowerCase());
    }

    @Override
    public boolean hasAttribute(String name) {
        requireNotEmpty(name);
        return attributes.containsKey(name.toLowerCase());
    }

    @Override
    public Resource getLogs() {
        net.microfalx.lang.Logger finalLogger = net.microfalx.lang.Logger.create();
        finalLogger.info("#### Session");
        finalLogger.append(logger);
        finalLogger.append(getToolsDescription());
        collectMessages(logger);
        collectProcessLogs(logger);
        return Resource.text(finalLogger.getOutput());
    }

    public void updateName(String name) {
        doUpdateName(name);
        nameChanged.set(true);
    }

    public final <CM extends ChatModel> AbstractChat setChatModel(CM chatModel) {
        this.chatModel = chatModel;
        return this;
    }

    public void ping() {
        // subclasses might implement some sort of ping with the model
        updateLastActivity();
    }

    protected void doClose() throws IOException {
        // default implementation does nothing
    }

    protected void collectMessages(net.microfalx.lang.Logger logger) {

    }

    protected void collectProcessLogs(net.microfalx.lang.Logger logger) {
        // subclasses can return other likes, like external process execution logs,  etc.
    }

    protected final File resolveModel() {
        return service.resolve(model);
    }

    private void validate() {
        if (chatModel == null) {
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
        this.chatMemory = service.getChatMemory();
        this.tokenCountEstimator = new JTokkitTokenCountEstimator();
        client = createClient();
        streamCompleted(new TokenStreamImpl(Collections.emptyIterator()));
        if (isNotEmpty(prompt.getQuestion())) summarize(prompt.getQuestion());
    }

    void streamCompleted(net.microfalx.bootstrap.ai.api.TokenStream tokenStream) {
        requireNonNull(tokenStream);
        inputTokenCount.addAndGet(tokenStream.getInputTokenCount());
        outputTokenCount.addAndGet(tokenStream.getOutputTokenCount());
        timeToFirstTokenTotal.addAndGet(tokenStream.getTimeToFirstToken().toMillis());
        timeToFirstTokenCount.incrementAndGet();
        summarize();
        updateDescription();
        changed.set(true);
        service.persistChatAsync(this);
    }

    void registerToolExecution(Tool.ExecutionRequest request, Tool.ExecutionResponse response) {
        requireNonNull(request);
        requireNonNull(response);
        toolExecutions.put(request, response);
    }

    void registerToolExecution(Tool.ExecutionRequest request, Throwable throwable) {
        requireNonNull(request);
        toolExecutionFailures.put(request, throwable);
    }

    private void initializePrincipal() {
        principal = SecurityContext.get().getPrincipal();
        setName(DEFAULT_NAME + String.format(" %03d", getNextChatIndex()));
    }

    private void doUpdateName(String name) {
        if (StringUtils.isNotEmpty(name)) setName(name);
    }

    private void summarize(String text) {
        if (nameChanged.get() || internal.get() || !nameChangePending.compareAndSet(false, true)) {
            return;
        }
        service.getChatPool().execute(() -> {
            try {
                String summarize = service.summarize(text, true);
                doUpdateName(summarize);
                nameChanged.set(true);
            } finally {
                nameChangePending.set(false);
            }
        });
    }

    private void summarize() {
        if (nameChanged.get()) return;
        Iterator<String> iterator = getMessages(false).stream().filter(message -> message.getType() == Message.Type.USER)
                .map(Message::getText).filter(StringUtils::isNotEmpty).iterator();
        StringBuilder builder = new StringBuilder();
        if (iterator.hasNext()) {
            builder.append("First Question: ").append(iterator.next());
        }
        if (iterator.hasNext()) {
            builder.append("\nSecond Question: ").append(iterator.next());
        }
        if (builder.isEmpty()) return;
        summarize(builder.toString().trim());
    }

    private ChatOptions createChatOptions() {
        ChatOptions.Builder builder = ChatOptions.builder()
                .model(model.getModelName())
                .temperature(model.getTemperature());
        return builder.build();
    }

    private ChatClient createClient() {
        ChatClient.Builder builder = ChatClient.builder(chatModel).defaultOptions(createChatOptions());
        builder.defaultSystem(new SystemMessageProvider())
                .defaultToolCallbacks(new ToolProvider())
                .defaultAdvisors(new AdvisorProvider());
        return builder.build();
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

    private List<Advisor> getAdvisors() {
        List<Advisor> advisors = new ArrayList<>();
        advisors.add(PromptChatMemoryAdvisor.builder(chatMemory).conversationId(getId()).build());
        return advisors;
    }

    private ToolCallback[] getFinalTools() {
        ToolCallback[] tools = new ToolCallback[0];
        if (!disableTools) tools = new ToolsBuilder(service, AbstractChat.this).getTools();
        return tools;
    }

    private String doGetSystemMessage() {
        return service.getSystemMessage(AbstractChat.this);
    }

    private class SystemMessageProvider implements Consumer<ChatClient.PromptSystemSpec> {

        @Override
        public void accept(ChatClient.PromptSystemSpec promptSystemSpec) {
            if (systemMessage == null) {
                systemMessage = doGetSystemMessage();
                promptSystemSpec.text(systemMessage);
                updateDescription();
            }
        }

    }

    private class AdvisorProvider implements Consumer<ChatClient.AdvisorSpec> {

        @Override
        public void accept(ChatClient.AdvisorSpec advisorSpec) {
            advisorSpec.advisors(getAdvisors());
        }
    }

    private class ToolProvider implements ToolCallbackProvider {

        @Override
        public ToolCallback[] getToolCallbacks() {
            return getFinalTools();
        }

    }

}
