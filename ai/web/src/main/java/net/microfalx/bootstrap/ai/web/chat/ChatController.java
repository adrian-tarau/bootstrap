package net.microfalx.bootstrap.ai.web.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.bootstrap.ai.api.*;
import net.microfalx.bootstrap.ai.core.MessageImpl;
import net.microfalx.bootstrap.dataset.DataSetRequest;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.help.HelpUtilities;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.security.SecurityContext;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.controller.PageController;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Name;
import net.microfalx.resource.Resource;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.bootstrap.ai.api.Token.Type.*;
import static net.microfalx.bootstrap.ai.core.AiUtils.getChatThreadPool;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;
import static net.microfalx.lang.ExceptionUtils.rethrowException;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.ThreadUtils.sleepMillis;
import static net.microfalx.lang.TimeUtils.FIVE_SECONDS;
import static net.microfalx.lang.TimeUtils.millisSince;

@Controller()
@RequestMapping("/ai/chat")
@Help("ai/chat")
@Name("Chat")
public class ChatController extends PageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
    private static final String END_OF_DATA = "$END_OF_DATA$";
    private static final Map<String, TokenStream> chatAnswer = new ConcurrentHashMap<>();

    @Autowired private HelpService helpService;
    @Autowired private AiService aiService;
    @Autowired private DataSetService dataSetService;

    @GetMapping("")
    public String start(Model model) {
        return doStart(model, aiService.getDefaultPrompt(), ChatTools.Mode.DASHBOARD, null, null);
    }

    @GetMapping("prompt/{id}")
    public String startDefaultModel(Model model, @PathVariable("id") String promptId,
                                    @RequestParam(value = "dataSet", required = false) String dataSetId) {
        Prompt prompt = aiService.getPrompt(promptId);
        model.addAttribute("title", prompt.getName());
        model.addAttribute("question", renderMarkdown(prompt.getQuestion()));
        DataSetRequest<?, ?, ?> request = getDataSetRequest(dataSetId);
        return doStart(model, prompt, ChatTools.Mode.DIALOG, null, request);
    }

    @GetMapping("model/{id}")
    public String startModel(Model model, @PathVariable("id") String modelId,
                             @RequestParam(value = "prompt", required = false) String promptId,
                             @RequestParam(value = "dataSet", required = false) String dataSetId) {
        net.microfalx.bootstrap.ai.api.Model chatModel = aiService.getModel(promptId);
        model.addAttribute("title", chatModel.getName());
        Prompt prompt = isNotEmpty(promptId) ? aiService.getPrompt(promptId) : aiService.getDefaultPrompt();
        DataSetRequest<?, ?, ?> request = getDataSetRequest(dataSetId);
        return doStart(model, prompt, ChatTools.Mode.DIALOG, chatModel, request);
    }

    @GetMapping("info/model/{id}")
    public String showModel(Model model, @PathVariable("id") String chatId) {
        net.microfalx.bootstrap.ai.api.Chat chat = aiService.getChat(chatId);
        updateModel(model, chat);
        model.addAttribute("title", "Model");
        model.addAttribute("content", renderMarkdown(chat.getDescription()));
        return "ai/chat :: info";
    }

    @GetMapping("info/tools/{id}")
    public String showTools(Model model, @PathVariable("id") String chatId) {
        net.microfalx.bootstrap.ai.api.Chat chat = aiService.getChat(chatId);
        updateModel(model, chat);
        model.addAttribute("title", "Tools");
        model.addAttribute("modalClasses", "modal-lg");
        model.addAttribute("content", renderMarkdown(chat.getToolsDescription()));
        return "ai/chat :: info";
    }

    @GetMapping("info/prompt/{id}")
    public String showPrompt(Model model, @PathVariable("id") String chatId) {
        net.microfalx.bootstrap.ai.api.Chat chat = aiService.getChat(chatId);
        updateModel(model, chat);
        model.addAttribute("title", "Prompt");
        model.addAttribute("content", chat.getSystemMessage().getText());
        model.addAttribute("modalClasses", "modal-lg");
        model.addAttribute("contentClasses", "ai-chat-info-smaller dataset-text-mono");
        return "ai/chat :: info";
    }

    @PostMapping(value = "question/{id}", consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String question(Model model, @PathVariable("id") String chatId, @RequestBody String message) {
        net.microfalx.bootstrap.ai.api.Chat chat = aiService.getChat(chatId);
        TokenStream stream = chat.chat(message);
        chatAnswer.put(chatId, stream);
        updateModel(model, chat);
        List<Message> messages = new ArrayList<>();
        model.addAttribute("messages", messages);
        messages.add(MessageImpl.create(Message.Type.USER, message));
        String waitingMessage = stream.isThinking() ? "Thinking..." : "Waiting for reply...";
        messages.add(MessageImpl.create(Message.Type.MODEL, waitingMessage));
        return "ai/chat :: question";
    }

    @GetMapping("tokens/{id}")
    public SseEmitter tokens(@PathVariable("id") String chatId) {
        net.microfalx.bootstrap.ai.api.Chat chat = aiService.getChat(chatId);
        TokenStream stream = chatAnswer.get(chatId);
        SseEmitter emitter = new SseEmitter();
        ThreadPool threadPool = getChatThreadPool(aiService);
        threadPool.execute(new ChatMessageTask(emitter, chat, stream));
        return emitter;
    }

    private String doStart(Model model, Prompt prompt, ChatTools.Mode mode, net.microfalx.bootstrap.ai.api.Model chatModel,
                           DataSetRequest<?, ?, ?> request) {
        if (chatModel == null) {
            if (prompt.getModel() != null) {
                chatModel = prompt.getModel();
            } else {
                chatModel = aiService.getDefaultModel();
            }
        }
        net.microfalx.bootstrap.ai.api.Chat chat = aiService.createChat(prompt, chatModel);
        chat.addFeature(request);
        updateModel(model, chat);
        updateIntro(model);
        model.addAttribute("mode", mode);
        if (mode == ChatTools.Mode.DASHBOARD) {
            return "ai/dashboard";
        } else {
            return "ai/chat::dialog";
        }
    }

    private DataSetRequest<?, ?, ?> getDataSetRequest(String dataSetId) {
        DataSetRequest<?, ?, ?> request = null;
        if (StringUtils.isNotEmpty(dataSetId)) {
            request = dataSetService.getRequest(dataSetId);
        }
        return request;
    }

    private String renderMarkdown(String text) {
        return renderMarkdown(Resource.text(text));
    }

    private String renderMarkdown(Resource resource) {
        try {
            return helpService.render(resource);
        } catch (IOException e) {
            LOGGER.atError().setCause(e).log("Failed to render markdown: " + resource.toURI());
            return "#Error: content not available";
        }
    }

    private void updateModel(Model model, net.microfalx.bootstrap.ai.api.Chat chat) {
        requireNonNull(chat);
        updateHelp(model);
        Collection<Chat> chats = aiService.getChats(SecurityContext.get().getPrincipal(), false);
        model.addAttribute("chat", chat);
        model.addAttribute("chats", chats);
        model.addAttribute("chatTools", new ChatTools(aiService, helpService, chat));
        updateMenu(model);
    }

    private void updateIntro(Model model) {
        StringWriter writer = new StringWriter();
        try {
            helpService.render(HelpUtilities.resolveContent("ai/chat-intro"), writer);
            Resource resource = aiService.applyTemplate(Resource.text(writer.toString()), Collections.emptyMap());
            model.addAttribute("chatIntro", resource.loadAsString());
        } catch (IOException e) {
            LOGGER.error("Failed to render chat intro", e);
        }
    }

    private void updateMenu(Model model) {
        Menu menu = new Menu();
        menu.add(new Item().setAction("chat.info.model").setText("Model").setIcon("fa-solid fa-square-binary")
                .setDescription("Displays information about the model"));
        menu.add(new Item().setAction("chat.info.tools").setText("Tools").setIcon("fa-solid fa-hammer")
                .setDescription("Displays information about the tools available for this chat and their usage"));
        menu.add(new Item().setAction("chat.info.prompt").setText("Prompt").setIcon("fa-solid fa-terminal")
                .setDescription("Displays information about the prompt"));
        model.addAttribute("chatInfoMenu", menu);
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Token {
        private final int index;
        private final String token;
    }

    private static class ChatMessageTask implements Runnable {

        private final SseEmitter emitter;
        private final net.microfalx.bootstrap.ai.api.Chat chat;
        private final TokenStream stream;
        private final AtomicInteger index = new AtomicInteger(1);
        private final ObjectMapper objectMapper;
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private volatile Throwable throwable;
        private volatile long lastTokenSent = System.currentTimeMillis();
        private volatile long lastPingSent = System.currentTimeMillis();

        public ChatMessageTask(SseEmitter emitter, net.microfalx.bootstrap.ai.api.Chat chat, TokenStream stream) {
            this.emitter = emitter;
            this.emitter.onCompletion(this::onCompletion);
            this.emitter.onError(this::onError);
            this.chat = chat;
            this.stream = stream;
            this.objectMapper = new ObjectMapper();
        }

        private void sendToken(String token, boolean asText) {
            SseEmitter.SseEventBuilder builder = SseEmitter.event().id(chat.getId());
            try {
                String data = token;
                if (!asText) {
                    data = objectMapper.writeValueAsString(new Token(index.getAndIncrement(), token));
                }
                builder.data(data);
                emitter.send(builder);
            } catch (IllegalStateException e) {
                rethrowException(e);
            } catch (Exception e) {
                LOGGER.error("Failed to send token for chat: {}", chat.getId(), e);
            }
        }

        private void onCompletion() {
            completed.set(true);
        }

        private void onError(Throwable throwable) {
            completed.set(true);
            this.throwable = throwable;
        }

        private String getPrefix(net.microfalx.bootstrap.ai.api.Token token) {
            if (token.getType() == ANSWER) {
                return "A:";
            } else if (token.getType() == THINKING) {
                return "T:";
            } else if (token.getType() == QUESTION) {
                return "Q:";
            } else if (token.getType() == PING) {
                return "P:";
            } else {
                return EMPTY_STRING;
            }
        }

        private void sendPingIfNeeded() {
            net.microfalx.bootstrap.ai.api.Token token = net.microfalx.bootstrap.ai.api.Token.create(PING, ".");
            if (millisSince(lastPingSent) > FIVE_SECONDS) {
                sendToken(getEncodedToken(token), true);
                chat.ping();
                lastPingSent = System.currentTimeMillis();
            }
        }

        private String getEncodedToken(net.microfalx.bootstrap.ai.api.Token token) {
            return getPrefix(token) + token.getText();
        }

        @Override
        public void run() {
            if (stream == null) {
                emitter.completeWithError(new IllegalStateException("No message stream available for chat: " + chat.getId()));
            } else {
                try {
                    while (!(stream.isComplete() || completed.get())) {
                        while (stream.hasNext()) {
                            sendToken(getEncodedToken(stream.next()), false);
                            lastTokenSent = System.currentTimeMillis();
                        }
                        sendPingIfNeeded();
                        sleepMillis(50);
                    }
                    if (stream.isComplete()) sendToken(END_OF_DATA, true);
                } catch (IllegalStateException e) {
                    throwable = e;
                } catch (Exception e) {
                    throwable = e;
                    emitter.completeWithError(e);
                } finally {
                    emitter.complete();
                }
            }
            if (throwable != null) {
                if (throwable instanceof IllegalStateException) {
                    LOGGER.info("Communication error with client for chat '{}', reason: {}", chat.getId(), getRootCauseDescription(throwable));
                } else {
                    LOGGER.warn("Error while processing chat {}, root cause: {}", chat.getId(), getRootCauseDescription(throwable));
                }
            }
        }
    }
}
