package net.microfalx.bootstrap.ai.core.provider.djl;

import net.microfalx.bootstrap.ai.api.*;
import net.microfalx.bootstrap.ai.core.AiServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DjlChatTest {

    @InjectMocks
    private AiServiceImpl llmService;

    private Provider provider;

    @BeforeEach
    void setup() throws Exception {
        provider = new DjlProviderFactory().createProvider();
    }

    @Test
    void ask() {
        Chat chat = llmService.createChat(loadModel("jlama_llama3_2_1b"));
        String response = chat.ask("Tell me a joke about Java");
        System.out.println(response);
        Assertions.assertThat(response.length()).isGreaterThan(0);
    }

    @Test
    void chat() {
        Chat chat = llmService.createChat(loadModel("jlama_llama3_2_1b"));
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

    private Model loadModel(String modelId) {
        return provider.getModel(modelId);
    }

}