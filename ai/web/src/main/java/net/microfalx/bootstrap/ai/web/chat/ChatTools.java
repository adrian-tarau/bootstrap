package net.microfalx.bootstrap.ai.web.chat;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiService;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Message;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.security.SecurityUtils;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.util.Collection;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;

@Slf4j
public class ChatTools {

    private final AiService aiService;
    private final HelpService helpService;
    private final Chat chat;

    public ChatTools(AiService aiService, HelpService helpService, Chat chat) {
        requireNonNull(aiService);
        requireNonNull(helpService);
        this.aiService = aiService;
        this.helpService = helpService;
        this.chat = chat;
    }

    public String getMessageCssClass(Message message) {
        if (message.getType() == Message.Type.USER) {
            return EMPTY_STRING;
        } else {
            return "end";
        }
    }

    public String getMessageImageCssClass(Message message) {
        return switch (message.getType()) {
            case USER -> "fa-solid fa-user-tie";
            case MODEL -> "fa-solid fa-robot";
            case SYSTEM -> "fa-solid fa-terminal";
            case CUSTOM -> "fa-solid fa-comment";
            default -> EMPTY_STRING;
        };
    }

    public Collection<Message> getMessages(Chat chat) {
        return chat.getMessages();
    }

    public String getUser(Message message) {
        if (message.getType() == Message.Type.USER) {
            return SecurityUtils.getDisplayName(chat.getUser());
        } else {
            return aiService.getName();
        }
    }

    public String renderMessageText(Message message) {
        try {
            return helpService.render(Resource.text(message.getText()));
        } catch (IOException e) {
            LOGGER.error("Failed to render message text for chat: {}", chat.getId(), e);
            return "#Error: failed to render message text";
        }
    }

    public enum Mode {
        DASHBOARD, DIALOG
    }
}
