package net.microfalx.bootstrap.ai.web.system;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.ai.api.AiService;
import net.microfalx.bootstrap.ai.api.Message;
import net.microfalx.bootstrap.ai.core.MessageImpl;
import net.microfalx.bootstrap.ai.web.chat.ChatTools;
import net.microfalx.bootstrap.ai.web.system.jpa.Chat;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Types;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.resource.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.List;

@Controller("SystemChatController")
@RequestMapping("/system/ai/chat")
@DataSet(model = Chat.class, viewTemplate = "ai/chat_view", viewClasses = "modal-lg")
@Slf4j
public class ChatController extends SystemDataSetController<Chat,Integer> {

    @Autowired private HelpService helpService;
    @Autowired private AiService aiService;

    public ChatController(DataSetService dataSetService) {
        super(dataSetService);
    }

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<Chat, Field<Chat>, Integer> dataSet, Model controllerModel, Chat dataSetModel) {
        super.beforeView(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) {
            net.microfalx.bootstrap.ai.api.Chat chat = aiService.getChat(dataSetModel.getId());
            Collection<Message> messages;
            try {
                messages = Types.asCollection(ResourceFactory.resolve(dataSetModel.getMemoryUri()), Message.class);
            } catch (Exception e) {
                messages = List.of(MessageImpl.create(Message.Type.SYSTEM, "Memory is not available"));
                LOGGER.atError().setCause(e).log("Failed to load memory for chat {}", dataSetModel.getId());
            }
            controllerModel.addAttribute("messages", messages);
            try {
                controllerModel.addAttribute("prompt", helpService.render(ResourceFactory.resolve(dataSetModel.getPromptUri())));
            } catch (Exception e) {
                controllerModel.addAttribute("prompt", "Prompt is not available");
                LOGGER.atError().setCause(e).log("Failed to load prompt for chat {}", dataSetModel.getId());
            }
            try {
                controllerModel.addAttribute("tools", helpService.render(ResourceFactory.resolve(dataSetModel.getToolsUri())));
            } catch (Exception e) {
                controllerModel.addAttribute("tools", "Tools are not available");
                LOGGER.atError().setCause(e).log("Failed to load tools for chat {}", dataSetModel.getId());
            }
            try {
                controllerModel.addAttribute("logs", helpService.render(ResourceFactory.resolve(dataSetModel.getLogsUri())));
            } catch (Exception e) {
                controllerModel.addAttribute("logs", "Logs are not available");
                LOGGER.atError().setCause(e).log("Failed to load logs for chat {}", dataSetModel.getId());
            }
            controllerModel.addAttribute("chatTools", new ChatTools(aiService, helpService, chat));
            controllerModel.addAttribute("mode", ChatTools.Mode.DASHBOARD);
        }
    }
}
