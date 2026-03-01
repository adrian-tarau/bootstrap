package net.microfalx.bootstrap.ai.ollama;

import net.microfalx.bootstrap.ai.api.*;
import net.microfalx.bootstrap.ai.core.AiPersistence;
import net.microfalx.bootstrap.ai.core.AiProperties;
import net.microfalx.bootstrap.ai.core.AiServiceImpl;
import net.microfalx.bootstrap.ai.core.jpa.ChatRepository;
import net.microfalx.bootstrap.ai.core.jpa.ModelRepository;
import net.microfalx.bootstrap.ai.core.jpa.PromptRepository;
import net.microfalx.bootstrap.ai.core.jpa.ProviderRepository;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.test.AbstractBootstrapServiceTestCase;
import net.microfalx.bootstrap.test.answer.RepositoryAnswer;
import net.microfalx.threadpool.ThreadPool;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ContextConfiguration(classes = {AiPersistence.class, AiProperties.class, AiServiceImpl.class,
        MetadataService.class, DataSetService.class})
class OllamaChatTest extends AbstractBootstrapServiceTestCase {

    @MockitoBean private ThreadPool threadPool;

    @TestBean private ProviderRepository providerRepository;
    @TestBean private ModelRepository modelRepository;
    @TestBean private PromptRepository promptRepository;
    @TestBean private ChatRepository chatRepository;

    @MockitoBean private IndexService indexService;
    @MockitoBean private SearchService searchService;

    @Autowired private AiProperties properties;
    @Autowired private AiServiceImpl aiService;

    private Prompt prompt;
    private Provider provider;

    @BeforeEach
    void setup() throws Exception {
        prompt = Prompt.empty();
        provider = new OllamaProviderFactory().setProperties(properties).createProvider();
        aiService.afterPropertiesSet();
    }

    @Test
    void ask() {
        Chat chat = aiService.createChat(prompt, loadChat("ollama_gemma3_1b"));
        String response = chat.ask("Tell me a joke about Java");
        System.out.println(response);
        Assertions.assertThat(response.length()).isGreaterThan(0);
    }

    @Test
    void chat() {
        Chat chat = aiService.createChat(prompt, loadChat("ollama_gemma3_1b"));
        chat(chat, "Tell me a joke about Java");
    }

    @Test
    void help() {
        Chat chat = aiService.createChat(prompt, loadChat("ollama_qwen3_1_7b"));
        chat(chat, "Which tools can you call?");
        chat(chat, "Can I receive emails with this application?");
    }

    @Test
    void tools() {
        aiService.registerTool(new DateTimeTools());
        Chat chat = aiService.createChat(prompt, loadChat("ollama_qwen3_1_7b"));
        chat(chat, "What day is tomorrow?");
    }

    private int chat(Chat chat, String message) {
        int tokenCount = 0;
        System.out.println("Question: " + message);
        System.out.flush();
        TokenStream stream = chat.chat(message);
        System.out.print("Answer: ");
        while (stream.hasNext()) {
            Token token = stream.next();
            System.out.print(token.getText());
            System.out.flush();
            tokenCount++;
        }
        System.out.println();
        Assertions.assertThat(tokenCount).isGreaterThan(0);
        return tokenCount;
    }

    private Model loadChat(String modelId) {
        return provider.getModel(modelId);
    }

    private static ProviderRepository providerRepository() {
        return RepositoryAnswer.mock(ProviderRepository.class);
    }

    private static ModelRepository modelRepository() {
        return RepositoryAnswer.mock(ModelRepository.class);
    }

    private static PromptRepository promptRepository() {
        return RepositoryAnswer.mock(PromptRepository.class);
    }

    private static ChatRepository chatRepository() {
        return RepositoryAnswer.mock(ChatRepository.class);
    }

    static class DateTimeTools {

        @Tool(description = "Get the current date and time in the user's timezone")
        String getCurrentDateTime() {
            return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        }

        @Tool(description = "Set a user alarm for the given time, provided in ISO-8601 format")
        void setAlarm(String time) {
            LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
            System.out.println("Alarm set for " + alarmTime);
        }

    }

}