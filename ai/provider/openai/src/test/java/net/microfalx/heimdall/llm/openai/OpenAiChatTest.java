package net.microfalx.bootstrap.ai.openai;

import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Provider;
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
class OpenAiChatTest {

    @InjectMocks
    private AiServiceImpl llmService;

    private AiProperties properties = new AiProperties();

    private Provider provider;

    @BeforeEach
    void setUp() {
        properties.setOpenAiApiKey(System.getProperty("openai.api_key", "demo"));
        properties.setOpenAiProjectId("proj_3CLMtXB2QZ5t4nl9CqOx9N8B");
        provider = new OpenAiProviderFactory().setProperties(properties).createProvider();
    }

    @Test
    void ask() {
        Chat chat = llmService.createChat(loadChat("openai_gpt_4_1_nano"));
        String response = chat.ask("Tell me a joke about Java");
        System.out.println(response);
        Assertions.assertThat(response.length()).isGreaterThan(0);
    }

    @Test
    void chat() {
        Chat chat = llmService.createChat(loadChat("openai_gpt_4_1_nano"));
        int tokenCount = 0;
        Iterator<String> stream = chat.chat("Tell me a joke about Java");
        while (stream.hasNext()) {
            String token = stream.next();
            System.out.print(token);
            System.out.flush();
            tokenCount++;
        }
        Assertions.assertThat(tokenCount).isGreaterThan(0);
    }

    private Model loadChat(String modelId) {
        return provider.getModel(modelId);
    }

}