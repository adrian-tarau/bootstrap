package net.microflax.bootstrap.ai.github;

import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.bootstrap.ai.core.AbstractChat;

public class GithubChat extends AbstractChat {

    public GithubChat(Prompt prompt, Model model) {
        super(prompt, model);
    }
}
