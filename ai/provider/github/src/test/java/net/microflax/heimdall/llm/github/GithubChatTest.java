package net.microflax.bootstrap.ai.github;

import net.microfalx.bootstrap.ai.api.*;
import net.microfalx.bootstrap.ai.core.AiProperties;
import net.microfalx.bootstrap.ai.core.AiServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;

@ExtendWith(MockitoExtension.class)
class GithubChatTest {

    @InjectMocks
    private AiServiceImpl llmService;

    private final AiProperties properties = new AiProperties();
    // Use a demo API key for testing purposes
    private Provider provider;

    @BeforeEach
    void setUp() {
        properties.setGitHubApiKey(System.getProperty("github.api_key", "demo"));
        provider = new GithubProviderFactory().setProperties(properties).createProvider();
    }

    @Test
    void ask() {
        Chat chat = llmService.createChat(loadChat("github_gpt_4o"));
        String response = chat.ask("Tell me a joke about Java");
        System.out.println(response);
        Assertions.assertThat(response.length()).isGreaterThan(0);
    }

    @Test
    void chat() {
        Chat chat = llmService.createChat(loadChat("github_gpt_4o"));
        int tokenCount = 0;
        TokenStream stream = chat.chat("Tell me a joke about Java");
        while (stream.hasNext()) {
            Token token = stream.next();
            System.out.print(token.getText());
            System.out.flush();
            tokenCount++;
        }
        Assertions.assertThat(tokenCount).isGreaterThan(0);
    }

    private Model loadChat(String modelId) {
        return provider.getModel(modelId);
    }
}