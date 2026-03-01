package net.microfalx.bootstrap.ai.core.provider.djl;

import net.microfalx.bootstrap.ai.api.AiNotFoundException;
import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChatFactory;
import net.microfalx.lang.StringUtils;

public class DjlChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new AiNotFoundException("The model name is required for JLama");
        }
        DjlChatModel chatModel = new DjlChatModel();
        chatModel.init(prompt);
        return new DjlChat(prompt, model).setChatModel(null);
    }
}
