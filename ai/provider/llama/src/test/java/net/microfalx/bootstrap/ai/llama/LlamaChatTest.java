package net.microfalx.bootstrap.ai.llama;

import net.microfalx.bootstrap.ai.api.*;
import net.microfalx.bootstrap.ai.core.AiPersistence;
import net.microfalx.bootstrap.ai.core.AiProperties;
import net.microfalx.bootstrap.ai.core.AiServiceImpl;
import net.microfalx.bootstrap.ai.core.jpa.ChatRepository;
import net.microfalx.bootstrap.ai.core.jpa.PromptRepository;
import net.microfalx.bootstrap.ai.core.jpa.ProviderRepository;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.test.AbstractBootstrapServiceTestCase;
import net.microfalx.bootstrap.test.answer.RepositoryAnswer;
import net.microfalx.threadpool.ThreadPool;
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

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {AiPersistence.class, AiProperties.class, AiServiceImpl.class,
        MetadataService.class, DataSetService.class})
class LlamaChatTest extends AbstractBootstrapServiceTestCase {

    @MockitoBean private ThreadPool threadPool;

    @TestBean private ProviderRepository providerRepository;
    @TestBean private net.microfalx.bootstrap.ai.core.jpa.ModelRepository modelRepository;
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
        aiService.registerProvider(new LlamaProviderFactory().createProvider());
        aiService.registerTool(new DateTimeTools());
        provider = aiService.getProvider("djl");
    }

    @Test
    void ask() {
        Chat chat = aiService.createChat(prompt, loadChat("djl_qwen2_5_0_5b"));
        ask(chat, "Tell me a joke about Java");
    }

    @Test
    void replyWithOk() {
        Chat chat = aiService.createChat(prompt, loadChat("djl_qwen2_5_0_5b"));
        ask(chat, "Reply with exactly the word: OK");
    }

    @Test
    void replyWithJson() {
        Chat chat = aiService.createChat(prompt, loadChat("djl_qwen2_5_0_5b"));
        ask(chat, "Return JSON with { status: 'up' }");
    }

    @Test
    void summarize() {
        Chat chat = aiService.createChat(prompt, loadChat("djl_qwen2_5_0_5b"));
        ask(chat, "Summarize Domain Events in one sentence");
    }

    @Test
    void handleSchema() {
        Chat chat = aiService.createChat(prompt, loadChat("djl_qwen2_5_0_5b"));
        ask(chat, """
                Return ONLY valid JSON.
                {
                  "customerName": string,
                  "address": string,
                  "zipCode": string
                }
                
                Customer: John Smith, 123 Main St, 45011""");
    }

    @Test
    void checkAddressTool() {
        aiService.registerTool(new AddressTools());
        Chat chat = aiService.createChat(prompt, loadChat("djl_qwen2_5_0_5b"));
        ask(chat, "Check if 123 Main St, Middletown, OH 45044 exists.");
    }

    @Test
    void checkTime() {
        aiService.registerTool(new AddressTools());
        Chat chat = aiService.createChat(prompt, loadChat("djl_qwen2_5_0_5b"));
        ask(chat, "What time is right now?");
    }

    @Test
    void chat() {
        Chat chat = aiService.createChat(prompt, loadChat("djl_qwen2_5_0_5b"));
        chat(chat, "Tell me a joke about Java");
    }

    private void ask(Chat chat, String message) {
        System.out.println("Question: " + message);
        System.out.flush();
        String response = chat.ask(message);
        System.out.print("Answer: ");
        System.out.println(response);
        assertThat(response.length()).isGreaterThan(0);
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
        assertThat(tokenCount).isGreaterThan(0);
        return tokenCount;
    }

    private Model loadChat(String modelId) {
        return provider.getModel(modelId);
    }

    private static ProviderRepository providerRepository() {
        return RepositoryAnswer.mock(ProviderRepository.class);
    }

    private static net.microfalx.bootstrap.ai.core.jpa.ModelRepository modelRepository() {
        return RepositoryAnswer.mock(net.microfalx.bootstrap.ai.core.jpa.ModelRepository.class);
    }

    private static PromptRepository promptRepository() {
        return RepositoryAnswer.mock(PromptRepository.class);
    }

    private static ChatRepository chatRepository() {
        return RepositoryAnswer.mock(ChatRepository.class);
    }

    static class DateTimeTools {

        @org.springframework.ai.tool.annotation.Tool(description = "Get the current date and time in the user's timezone")
        String getCurrentDateTime() {
            return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        }

        @Tool(description = "Set a user alarm for the given time, provided in ISO-8601 format")
        void setAlarm(String time) {
            LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
            System.out.println("Alarm set for " + alarmTime);
        }

    }

    public record CheckAddressRequest(
            String street,
            String city,
            String state,
            String zip
    ) {
    }

    static class AddressTools {

        @Tool(name = "checkAddress", description = "Checks if an address is a valid US address")
        String checkAddress(CheckAddressRequest request) {
            return "Address is valid";
        }
    }

}