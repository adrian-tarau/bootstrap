package net.microfalx.bootstrap.ai.core;

import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.test.AbstractBootstrapServiceTestCase;
import net.microfalx.bootstrap.test.answer.RepositoryAnswer;
import net.microfalx.bootstrap.ai.api.*;
import net.microfalx.bootstrap.ai.core.jpa.ModelRepository;
import net.microfalx.bootstrap.ai.core.jpa.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestPropertySource(properties = "heimdall.llm.persistenceEnabled=false")
@ContextConfiguration(classes = {AiServiceImpl.class, MetadataService.class, AiProperties.class})
class AiServiceImplTest extends AbstractBootstrapServiceTestCase {

    @TestBean private ModelRepository modelRepository;
    @TestBean private ProviderRepository providerRepository;

    @MockitoBean private IndexService indexService;
    @MockitoBean private SearchService searchService;

    @Autowired private AiProperties properties;
    @Autowired private AiService aiService;

    @Test
    void initialize() {
        assertThat(aiService.getModels().size()).isGreaterThan(1);
        assertNotNull(aiService.getModel("onnx_all_minilm_l6_v2_q").getTemperature());
        assertNotNull(aiService.getModel("onnx_all_minilm_l6_v2_q").getTopK());
        assertNotNull(aiService.getModel("onnx_all_minilm_l6_v2_q").getTopP());
    }

    @Test
    void embed() {
        assertEquals(384, aiService.embed("This is a test embedding").getDimension());
    }

    private static ModelRepository modelRepository() {
        return RepositoryAnswer.mock(ModelRepository.class);
    }

    private static ProviderRepository providerRepository() {
        return RepositoryAnswer.mock(ProviderRepository.class);
    }

    public static class TestChat extends AbstractChat {

        public TestChat(Prompt prompt, Model model) {
            super(prompt, model);
        }
    }

    public static class TestChatFactory implements net.microfalx.bootstrap.ai.api.Chat.Factory {

        @Override
        public Chat createChat(Prompt prompt, Model model) {
            return new TestChat(prompt, model);
        }
    }

    @net.microfalx.lang.annotation.Provider
    public static class TestProviderFactory implements net.microfalx.bootstrap.ai.api.Provider.Factory {

        @Override
        public Provider createProvider() {
            Provider.Builder builder = new Provider.Builder("test").chatFactory(new TestChatFactory());
            builder.model(Model.create("m1", "Model 1"));
            builder.model(Model.create("m2", "Model 2"));
            return builder.build();
        }
    }

}