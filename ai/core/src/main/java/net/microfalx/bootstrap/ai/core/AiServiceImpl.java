package net.microfalx.bootstrap.ai.core;

import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.Getter;
import net.microfalx.bootstrap.ai.api.*;
import net.microfalx.bootstrap.ai.core.repository.LocalRepository;
import net.microfalx.bootstrap.ai.lucene.LuceneEmbeddingModel;
import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.dataset.DataSetExport;
import net.microfalx.bootstrap.dataset.DataSetRequest;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.IndexListener;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.lang.*;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import net.microfalx.resource.rocksdb.RocksDbResource;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.template.ValidationMode;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.bootstrap.ai.core.AiUtils.CREATE_CHAT_METRICS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.rethrowException;
import static net.microfalx.lang.FileUtils.getFileName;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;
import static net.microfalx.lang.FormatterUtils.formatBytes;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.TimeUtils.millisSince;

@Service
public class AiServiceImpl extends ApplicationContextSupport implements AiService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiService.class);

    @Autowired
    private IndexService indexService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private AiPersistence persistence;

    @Autowired(required = false)
    @Getter(AccessLevel.PROTECTED)
    private AiProperties properties = new AiProperties();

    private File variableDirectory;
    private File cacheDirectory;
    private File modelCacheDirectory;
    private LocalRepository localRepository = new LocalRepository(new RepositoryProxy());
    private EmbeddingModel embeddingModel;
    private ChatMemory chatMemory;
    private volatile AiCache cache = new AiCache(this, null);
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();
    private final Map<String, Object> variables = new ConcurrentHashMap<>();

    private final Collection<AiListener> listeners = new CopyOnWriteArrayList<>();
    private final Collection<ModelRepository> modelRepositories = new CopyOnWriteArrayList<>();
    private final Collection<Provider.Factory> providerFactories = new CopyOnWriteArrayList<>();
    private final Map<String, net.microfalx.bootstrap.ai.api.Chat> activeChats = new ConcurrentHashMap<>();
    private final Collection<net.microfalx.bootstrap.ai.api.Chat> closedChats = new CopyOnWriteArrayList<>();

    private volatile Model defaultModel;
    private volatile Model defaultEmbeddingModel;

    private String defaultPromptId;

    private ThreadPool chatPool;
    private ThreadPool embeddingPool;

    private Resource chatsResource;
    private static final Map<String, Long> lastAutoSave = new ConcurrentHashMap<>();

    public AiServiceImpl() {
        // Workaround for classes not being loaded by Spring
        warmClassesWorkaround();
    }

    ChatMemory getChatMemory() {
        return chatMemory;
    }

    @Override
    public String getName() {
        return defaultIfEmpty(properties.getApplicationName(), "Martin");
    }

    @Override
    public Chat createChat() {
        return createChat(getDefaultPrompt());
    }

    @Override
    public Chat createChat(Prompt prompt) {
        requireNonNull(prompt);
        Model model = getDefaultModel();
        if (prompt.getModel() != null) model = prompt.getModel();
        return createChat(prompt, model);
    }

    @Override
    public Chat createChat(Model model) {
        return createChat(getDefaultPrompt(), model);
    }

    public net.microfalx.bootstrap.ai.api.Chat createChat(Prompt prompt, Model model) {
        return createChat(prompt, model, false);
    }

    @Override
    public Collection<Chat> getChats(Principal principal, boolean active) {
        requireNonNull(principal);
        Collection<Chat> chats = new ArrayList<>();
        if (active) {
            activeChats.values().forEach(chat -> {
                if (principal.equals(chat.getUser())) chats.add(chat);
            });
        }
        return chats;
    }

    @Override
    public Chat getChat(String id) {
        requireNonNull(id);
        Chat chat = activeChats.get(toIdentifier(id));
        if (chat == null) {
            throw new AiException("Chat '" + id + "' not found");
        }
        return chat;
    }

    @Override
    public Embedding embed(String text) {
        return createEmbedding(getDefaultEmbeddingModel(), text);
    }

    @Override
    public Embedding embed(String modelId, String text) {
        return createEmbedding(getModel(modelId), text);
    }

    @Override
    public String summarize(String text, boolean shortMessage) {
        if (StringUtils.isEmpty(text)) return EMPTY_STRING;
        return AiUtils.MISC_METRICS.time("Summarize", () -> {
            Chat chat = createChat(Prompt.empty(), getDefaultModel(), true);
            chat.disableTools();
            try {
                String question = shortMessage ? properties.getSummaryWords() : properties.getSummarySentence();
                question += "\n\nText to summarize:\n```\n" + text + "\n```";
                TokenStream stream = chat.chat(question);
                return stream.getAnswerMessage().getText();
            } finally {
                chat.close();
            }
        });
    }

    public Embedding createEmbedding(Model model, String text) {
        requireNonNull(model);
        if (!model.isEmbedding()) throw new AiException("Model '" + model.getId() + "' does not support embedding");
        return model.getProvider().getEmbeddingFactory().createEmbedding(model, text);
    }

    @Override
    public Model getDefaultModel() {
        if (defaultModel != null) return defaultModel;
        if (isNotEmpty(properties.getDefaultModel()) && isNotEmpty(properties.getDefaultProvider())) {
            String defaultModelId = properties.getDefaultProvider() + "." + properties.getDefaultModel();
            defaultModel = cache.findModel(defaultModelId);
            if (defaultModel == null) {
                LOGGER.warn("A model with identifier '{}' not found, falling back to configured model", defaultModelId);
            }
        }
        if (defaultModel == null) {
            defaultModel = getModels().stream().filter(model -> model.isDefault() && !model.isEmbedding())
                    .findFirst().orElseThrow(() -> new AiNotFoundException("No default model found"));
        }
        return defaultModel;
    }

    @Override
    public Model getDefaultEmbeddingModel() {
        if (defaultEmbeddingModel != null) return defaultEmbeddingModel;
        defaultEmbeddingModel = getModels().stream().filter(model -> model.isDefault() && model.isEmbedding()).findFirst().orElseThrow(
                () -> new AiNotFoundException("No default embedding model found"));
        return defaultEmbeddingModel;
    }

    @Override
    public Collection<Model> getModels() {
        return unmodifiableCollection(cache.getModels().values());
    }

    @Override
    public Model getModel(String id) {
        return cache.getModel(id);
    }

    @Override
    public File resolve(Model model) {
        requireNonNull(model);
        String cacheFileName = model.getId();
        if (model.getDownloadUri() != null) cacheFileName = getFileName(model.getDownloadUri().getPath());
        File modelCacheFile = new File(cacheDirectory, cacheFileName);
        if (modelCacheFile.exists()) {
            LOGGER.debug("Return model file from cache: {}", modelCacheFile);
            return modelCacheFile;
        }
        Resource resource = fireResolveModel(model);
        if (resource == null) {
            throw new AiNotFoundException("Model '" + model.getId() + "' cannot be resolved in any model repository");
        }
        LOGGER.info("Download model file from '{}' for model '{}'", resource.toURI(), model.getName());
        try {
            Resource.file(modelCacheFile).copyFrom(resource);
        } catch (IOException e) {
            throw new AiException("Failed to cache model '" + model.getId() + "' to file " + modelCacheFile, e);
        }
        LOGGER.info("Download model file to '{}', file size {}", modelCacheFile,
                formatBytes(modelCacheFile.length()));
        return modelCacheFile;
    }

    @Override
    public Collection<net.microfalx.bootstrap.ai.api.Chat> getActiveChats() {
        return unmodifiableCollection(activeChats.values());
    }

    @Override
    public Iterable<net.microfalx.bootstrap.ai.api.Chat> getHistoricalChats() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Provider> getProviders() {
        return unmodifiableCollection(cache.getProviders().values());
    }

    @Override
    public Provider getProvider(String id) {
        return cache.getProvider(id);
    }

    @Override
    public void registerProvider(Provider provider) {
        requireNonNull(provider);
        cache.registerProvider(provider);
        persistProvider(provider);
        if (provider.getChatFactory() instanceof AbstractChatFactory chatFactory) {
            chatFactory.setProperties(properties);
            chatFactory.setAiService(this);
        }
    }

    @Override
    public Collection<Tool> getTools() {
        return unmodifiableCollection(tools.values());
    }

    @Override
    public Tool getTool(String id) {
        requireNonNull(id);
        Tool tool = tools.get(toIdentifier(id));
        if (tool == null) throw new AiNotFoundException("Tool '" + id + "' not found");
        return tool;
    }

    @Override
    public void registerTool(Tool tool) {
        requireNonNull(tool);
        LOGGER.info("Registering tool {}", tool.getId());
        tools.put(toIdentifier(tool.getId()), tool);
        if (tool.getExecutor() instanceof ApplicationContextAware applicationContextAware) {
            applicationContextAware.setApplicationContext(getApplicationContext());
        }
    }

    @Override
    public void registerTool(Object tool) {
        requireNonNull(tool);
        LOGGER.info("Registering tool {}", ClassUtils.getName(tool));
        MethodToolCallbackProvider provider = MethodToolCallbackProvider.builder().toolObjects(tool).build();
        Arrays.asList(provider.getToolCallbacks()).forEach(callback -> registerTool(AiTools.fromToolCallback(callback)));
    }

    @Override
    public void registerVariable(String name) {
        requireNotEmpty(name);
        try {
            String content = ClassPathResource.file("ai/variables/" + toIdentifier(name) + ".md").loadAsString();
            registerVariable(name, content);
        } catch (IOException e) {
            ExceptionUtils.rethrowException(e);
        }
    }

    @Override
    public void registerVariable(String name, Object value) {
        requireNotEmpty(name);
        requireNonNull(value);
        variables.put(name, value);
    }

    @Override
    public Resource applyTemplate(Resource resource, Map<String, Object> variables) throws IOException {
        requireNonNull(resource);
        variables = ObjectUtils.defaultIfNull(variables, Collections.emptyMap());
        Map<String, Object> currentVariables = new HashMap<>(this.variables);
        currentVariables.putAll(variables);
        PromptTemplate template = createPromptTemplateBuilder().template(resource.loadAsString()).variables(currentVariables).build();
        String renderedTemplate = template.render();
        return Resource.text(renderedTemplate);
    }

    /**
     * Returns the final prompt text for the given model and prompt.
     *
     * @param chat the chat to use
     * @return a non-null string
     */
    public String getSystemMessage(Chat chat) {
        SystemMessageBuilder builder = new SystemMessageBuilder(this, chat.getModel(), chat.getPrompt());
        Map<String, Object> currentVariables = new HashMap<>(this.variables);
        updateToolsVariable(chat, currentVariables);
        return AiUtils.CREATE_SYSTEM_MESSAGE_METRICS.time(chat.getPrompt().getName(), () -> {
            getDataSetAsJson(chat, currentVariables);
            PromptTemplate template = createPromptTemplateBuilder().template(builder.build()).variables(currentVariables).build();
            return template.render();
        });
    }

    @Override
    public void reload() {
        if (!properties.isPersistenceEnabled()) return;
        AiCache cache = new AiCache(this, this.cache);
        cache.setApplicationContext(getApplicationContext());
        cache.load();
        this.defaultModel = null;
        this.defaultEmbeddingModel = null;
        this.cache = cache;
    }

    @Override
    public Prompt getDefaultPrompt() {
        return StringUtils.isNotEmpty(defaultPromptId) ? getPrompt(defaultPromptId) : Prompt.empty();
    }

    @Override
    public Collection<Prompt> getPrompts() {
        return unmodifiableCollection(cache.getPrompts().values());
    }

    @Override
    public Collection<Prompt> getPrompts(Collection<String> tags) {
        Collection<Prompt> filteredPrompts = new ArrayList<>();
        for (Prompt prompt : cache.getPrompts().values()) {
            for (String tag : tags) {
                if (prompt.getTags().contains(tag)) filteredPrompts.add(prompt);
            }
        }
        return filteredPrompts;
    }

    @Override
    public Collection<Prompt> getPrompts(String... tags) {
        return getPrompts(Arrays.asList(tags));
    }

    @Override
    public Prompt getPrompt(String id) {
        return cache.getPrompt(id);
    }

    @Override
    public void registerPrompt(Prompt prompt) {
        requireNonNull(prompt);
        cache.registerPrompt(prompt);
        persistence.execute(prompt);
        if (prompt.getTags().contains(Model.DEFAULT_TAG)) defaultPromptId = prompt.getId();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeApplicationContext();
        registerLibraryPaths();
        initThreadPools();
        initDirectories();
        initListeners();
        initModelRepositories();
        initProviderFactories();
        registerProviders();
        registerDefaultVariables();
        initializeChatStore();
        initializeEmbeddingStore();
        initResources();
        initTask();
    }

    @PreDestroy
    protected void destroy() {
        //if (embeddingModel != null) embeddingModel.close();
    }

    public ThreadPool getChatPool() {
        return ObjectUtils.defaultIfNull(chatPool, ThreadPool.get());
    }

    public ThreadPool getEmbeddingPool() {
        return ObjectUtils.defaultIfNull(embeddingPool, ThreadPool.get());
    }

    /**
     * Closes the given chat and persists it.
     *
     * @param chat the chat to close
     */
    void closeChat(Chat chat) {
        requireNonNull(chat);
        activeChats.remove(chat.getId());
        closedChats.add(chat);
        persistence.execute(chat);
    }

    /**
     * Returns any data set associated with the chat as a JSON string.
     *
     * @param chat the chat to get the data set from
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getDataSetAsJson(Chat chat, Map<String, Object> variables) {
        requireNonNull(chat);
        DataSetRequest request = chat.getFeature(DataSetRequest.class);
        if (request == null) {
            LOGGER.warn("No data set request found for chat {}", chat.getId());
            variables.put("DATASET", JSON_ERROR);
            variables.put("SCHEMA", JSON_ERROR);
        } else {
            AiUtils.MISC_METRICS.time("Get Data Set", (t) -> {
                dataSetService.doWithDataSet(request.getDataSet(), dataSet -> doGetDataSetAsJsonUnderTransaction(chat, variables));
            });
        }
    }

    Object doGetDataSetAsJsonUnderTransaction(Chat chat, Map<String, Object> variables) {
        DataSetRequest<?, ?, ?> request = chat.getFeature(DataSetRequest.class);
        Page<?> page = null;
        if (request != null) {
            page = fireGetDataSet(chat, request);
        }
        update(chat, request, "SCHEMA", true, page, variables);
        update(chat, request, "DATASET", false, page, variables);
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void update(Chat chat, DataSetRequest dataSetRequest, String name, boolean schema,
                        Page page, Map<String, Object> variables) {
        Resource resource;
        if (page != null) {
            DataSetExport<Object, Field<Object>, Object> export = DataSetExport.create(DataSetExport.Format.JSON)
                    .setIncludeMetadata(schema).setIncludeData(!schema);
            export.initialize(getApplicationContext());
            resource = export.export(dataSetRequest.getDataSet(), page);
        } else {
            LOGGER.warn("No data set found for chat {}", chat.getId());
            resource = Resource.text(JSON_ERROR);
        }
        String json = JSON_ERROR;
        try {
            json = resource.loadAsString();
        } catch (IOException e) {
            LOGGER.warn("Failed to load data set for chat {}", chat.getId());
        }
        variables.put(name, json);
    }

    /**
     * Returns a fragment of the prompt text.
     *
     * @param model    the model to use
     * @param prompt   the prompt to use
     * @param fragment the fragment to return
     * @param text the original text of the fragment (if any)
     * @return the text of the fragment, never null
     */
    String getPromptFragment(Model model, Prompt prompt, Prompt.Fragment fragment, String text) {
        requireNonNull(model);
        requireNonNull(prompt);
        requireNonNull(fragment);
        String originalText = text;
        for (AiListener listener : listeners) {
            text = listener.getFragment(model, prompt, fragment, text);
            if (isNotEmpty(text)) break;
        }
        return isEmpty(text) ? originalText : text;
    }

    /**
     * Returns a fragment title.
     *
     * @param model    the model to use
     * @param prompt   the prompt to use
     * @param fragment the fragment to return
     * @param title    the suggested title for the fragment
     * @return the text of the fragment, never null
     */
    String getTitle(Model model, Prompt prompt, Prompt.Fragment fragment, String title) {
        requireNonNull(model);
        requireNonNull(prompt);
        requireNonNull(fragment);
        String originalTitle = title;
        for (AiListener listener : listeners) {
            title = listener.getTitle(model, prompt, fragment, title);
            if (isNotEmpty(title)) break;
        }
        return isEmpty(title) ? originalTitle : title;
    }

    /**
     * Stores the chat messages serialized in an external resource.
     *
     * @param chat the snapshot
     * @return the resource where the snapshot was stored
     * @throws IOException I/O exception if snapshot cannot be stored
     */
    Resource writeChatMessages(Chat chat) throws IOException {
        Resource directory = chatsResource.resolve(DIRECTORY_DATE_FORMATTER.format(LocalDateTime.now()), Resource.Type.DIRECTORY);
        Resource target = directory.resolve(String.format(FILE_NAME_FORMAT, getNextSequence()));
        if (!directory.exists()) directory.create();
        return AiUtils.MISC_METRICS.timeCallable("Serialize Chat Messages", () -> {
            List<Message> messages = chatMemory.get(chat.getId());
            // TODO - optimize by writing directly to the output stream instead of building a string in memory
            String json = "";//ChatMessageSerializer.messagesToJson(messages);
            IOUtils.appendStream(target.getWriter(), new StringReader(json));
            return target;
        });

    }

    private net.microfalx.bootstrap.ai.api.Chat createChat(Prompt prompt, Model model, boolean internal) {
        requireNonNull(prompt);
        requireNonNull(model);
        if (model.isEmbedding()) throw new AiException("Model '" + model.getId() + "' does not support chatting");
        net.microfalx.bootstrap.ai.api.Chat result = CREATE_CHAT_METRICS.time(model.getName(), () -> {
            Chat.Factory chatFactory = model.getProvider().getChatFactory();
            net.microfalx.bootstrap.ai.api.Chat chat = chatFactory.createChat(prompt, model);
            if (!internal) activeChats.put(toIdentifier(chat.getId()), chat);
            if (chat instanceof AbstractChat abstractChat) {
                abstractChat.internal.set(internal);
                abstractChat.initialize(this);
            }
            return chat;
        });
        if (!internal) ThreadPool.get().execute(() -> persistence.execute(result));
        return result;
    }

    private void initDirectories() {
        variableDirectory = JvmUtils.getVariableDirectory("ai");
        cacheDirectory = JvmUtils.getCacheDirectory("ai");
        modelCacheDirectory = FileUtils.validateDirectoryExists(new File(cacheDirectory, "models"));
        localRepository.setCacheDirectory(modelCacheDirectory);
    }

    private void initListeners() {
        Collection<AiListener> loadedListeners = ClassUtils.resolveProviderInstances(AiListener.class);
        LOGGER.info("Register {} listeners", loadedListeners.size());
        for (AiListener listener : loadedListeners) {
            LOGGER.debug(" - {}", ClassUtils.getName(loadedListeners));
            if (listener instanceof ApplicationContextAware applicationContextAware) {
                applicationContextAware.setApplicationContext(getApplicationContext());
            }
            this.listeners.add(listener);
        }
    }

    private void initModelRepositories() {
        Collection<ModelRepository> loadedModelRepositories = ClassUtils.resolveProviderInstances(ModelRepository.class);
        LOGGER.info("Register {} model repositories", loadedModelRepositories.size());
        for (ModelRepository modelRepository : loadedModelRepositories) {
            LOGGER.debug(" - {}", ClassUtils.getName(loadedModelRepositories));
            if (modelRepository instanceof ApplicationContextAware applicationContextAware) {
                applicationContextAware.setApplicationContext(getApplicationContext());
            }
            this.modelRepositories.add(modelRepository);
        }
    }

    private void initProviderFactories() {
        Collection<Provider.Factory> loadedProviderFactories = ClassUtils.resolveProviderInstances(Provider.Factory.class);
        LOGGER.info("Register {} provider factories", loadedProviderFactories.size());
        for (Provider.Factory providerFactory : loadedProviderFactories) {
            LOGGER.debug(" - {}", ClassUtils.getName(providerFactory));
            this.providerFactories.add(providerFactory);
        }
    }

    private void initializeEmbeddingStore() {
        this.embeddingModel = new LuceneEmbeddingModel(this, indexService, searchService)
                .setThreadPool(getEmbeddingPool()).setEnabled(properties.isEmbeddingEnabled());
        this.indexService.registerListener((IndexListener) this.embeddingModel);
        //this.contentRetriever = this.embeddingStore.getContentRetriever();
    }

    private void initializeChatStore() {
        ChatMemoryRepository repository = new AiChatStore(persistence);
        this.chatMemory = MessageWindowChatMemory.builder().chatMemoryRepository(repository).maxMessages(1000).build();
    }

    private void initializeApplicationContext() {
        persistence.aiService = this;
        persistence.setApplicationContext(getApplicationContext());
        cache.setApplicationContext(getApplicationContext());
    }

    private void initResources() {
        chatsResource = getSharedResource().resolve("chats", Resource.Type.DIRECTORY);
        if (chatsResource.isLocal()) {
            LOGGER.info("Chat messages are stored in a RocksDB database: {}", chatsResource);
            Resource dbStatementsResource = RocksDbResource.create(chatsResource);
            try {
                dbStatementsResource.create();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize statement store", e);
                System.exit(10);
            }
            ResourceFactory.registerSymlink("ai/chats", dbStatementsResource);
        } else {
            LOGGER.info("Database statements are stored in a remote storage: {}", chatsResource);
        }
    }

    private void initThreadPools() {
        chatPool = ThreadPoolFactory.create("AI").create();
        embeddingPool = ThreadPoolFactory.create("Embedding").create();
    }

    private void persistProvider(Provider provider) {
        if (!properties.isPersistenceEnabled()) return;
        for (Model model : provider.getModels()) {
            persistence.execute(model);
        }
    }

    private void initTask() {
        ThreadPool threadPool = ThreadPool.get();
        threadPool.execute(this::fireStartEvent);
        threadPool.scheduleAtFixedRate(new MaintenanceTask(), Duration.ofSeconds(60));
        reloadAsync();
    }

    private void fireStartEvent() {
        for (AiListener listener : listeners) {
            try {
                listener.onStart(this);
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to notify listener {}", ClassUtils.getName(listener));
            }
        }
        reloadAsync();
    }

    private void reloadAsync() {
        ThreadPool.get().execute(this::reload);
    }

    private <M, F extends Field<M>, ID> Page<M> fireGetDataSet(Chat chat, DataSetRequest<M, F, ID> request) {
        for (AiListener listener : listeners) {
            try {
                Page<M> page = listener.getPage(chat, request);
                if (page != null) return page;
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to build data set result for {} with listener {}", chat.getName(), ClassUtils.getName(listener));
            }
        }
        return null;
    }

    private Resource fireResolveModel(Model model) {
        for (ModelRepository modelRepository : modelRepositories) {
            try {
                if (modelRepository.supports(model)) {
                    Resource resource = modelRepository.resolve(model);
                    if (resource != null) return resource;
                }
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to resolve model {} with listener {}",
                        model.getName(), ClassUtils.getName(modelRepository));
            }
        }
        return null;
    }

    private void updateToolsVariable(Chat chat, Map<String, Object> variables) {
        ToolsBuilder builder = new ToolsBuilder(this, chat);
        variables.put("TOOLS", builder.getVariable());
    }

    private Resource getSharedResource() {
        return resourceService.getShared("ai");
    }

    private void registerProviders() {
        for (Provider.Factory providerFactory : providerFactories) {
            try {
                if (providerFactory instanceof AbstractProviderFactory abstractProviderFactory) {
                    abstractProviderFactory.setProperties(properties);
                    abstractProviderFactory.setAiService(this);
                }
                Provider provider = providerFactory.createProvider();
                if (provider == null) {
                    LOGGER.error("Provider factory {} returned NULL", ClassUtils.getName(providerFactory));
                } else {
                    registerProvider(provider);
                }
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to create provider with factory {}", ClassUtils.getName(providerFactory));
            }
        }
    }

    private void warmClassesWorkaround() {
        try {
            Class.forName("net.microfalx.bootstrap.ai.lucene.LuceneContentRetriever");
            Class.forName("net.microfalx.bootstrap.ai.lucene.LuceneEmbeddingModel");
        } catch (Exception e) {
            rethrowException(e);
        }
    }

    private void registerLibraryPaths() {
        File djlCache = JvmUtils.getCacheDirectory("djl");
        System.setProperty("ENGINE_CACHE_DIR", validateDirectoryExists(new File(djlCache, "engine")).getAbsolutePath());
        System.setProperty("DJL_CACHE_DIR", validateDirectoryExists(new File(djlCache, "cache")).getAbsolutePath());
        System.setProperty("DJL_OFFLINE", Boolean.toString(properties.isOffline()));
    }

    private void registerDefaultVariables() {
        registerVariable("APP_NAME", getName());
    }

    private void processPendingChats() {
        for (Chat chat : activeChats.values()) {
            long lastActivity = ((AbstractChat) chat).lastActivity.get();
            boolean changed = ((AbstractChat) chat).changed.get();
            long lastAutoSaveForChat = lastAutoSave.computeIfAbsent(chat.getId(), s -> TimeUtils.oneHourAgo());
            if (changed && millisSince(lastAutoSaveForChat) > properties.getChatAutoSaveInterval().toMillis()) {
                persistence.execute(chat);
                lastAutoSave.put(chat.getId(), currentTimeMillis());
                ((AbstractChat) chat).changed.set(false);
            }
            if (millisSince(lastActivity) > properties.getChatTimeout().toMillis()) {
                LOGGER.info("Closing chat {} due to inactivity", chat.getId());
                chat.close();
            }
        }
    }

    private int getNextSequence() {
        return resourceSequences.computeIfAbsent(LocalDate.now(), localDate -> {
            int start = 1;
            if (STARTUP.toLocalDate().equals(localDate)) {
                start = STARTUP.toLocalTime().toSecondOfDay();
            }
            return new AtomicInteger(start);
        }).getAndIncrement();
    }

    private PromptTemplate.Builder createPromptTemplateBuilder() {
        StTemplateRenderer templateRenderer = StTemplateRenderer.builder().startDelimiterToken('{').endDelimiterToken('}')
                .validationMode(ValidationMode.WARN).build();
        return PromptTemplate.builder().renderer(templateRenderer);
    }

    class MaintenanceTask implements Runnable {

        @Override
        public void run() {
            try {
                processPendingChats();
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to process pending chats");
            }
        }
    }

    class RepositoryProxy implements Function<Model, Resource> {

        @Override
        public Resource apply(Model model) {
            return fireResolveModel(model);
        }
    }

    private static final String JSON_ERROR = "{message = 'No data set found'}";
    private static final DateTimeFormatter DIRECTORY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String FILE_NAME_FORMAT = "%09d";
    private static final LocalDateTime STARTUP = LocalDateTime.now();
    private static final Map<LocalDate, AtomicInteger> resourceSequences = new ConcurrentHashMap<>();
}
