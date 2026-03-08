package net.microfalx.bootstrap.ai.web.system;

import net.microfalx.bootstrap.ai.web.system.jpa.Chat;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemChatController")
@RequestMapping("/system/ai/chat")
@DataSet(model = Chat.class)
public class ChatController extends SystemDataSetController<Chat,Integer> {

    public ChatController(DataSetService dataSetService) {
        super(dataSetService);
    }

}
