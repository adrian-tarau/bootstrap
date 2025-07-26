package net.microfalx.bootstrap.ai.core.provider.onnx;

import net.microfalx.bootstrap.ai.api.Embedding;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OnnxEmbeddingFactoryTest {

    private Provider provider;

    @BeforeEach
    void setup() throws Exception {
        provider = new OnnxProviderFactory().createProvider();
    }

    @Test
    void embedMinLmv2q() {
        Embedding embedding = createEmbedding("onnx-all-minilm-l6-v2-q", "This is a test embedding");
        assertEquals(384, embedding.getDimension());
    }

    @Test
    void embedE5Smallq() {
        Embedding embedding = createEmbedding("onnx-e5-small-v2-q", "This is a test embedding");
        assertEquals(384, embedding.getDimension());
    }

    private Embedding createEmbedding(String modelId, String text) {
        OnnxEmbeddingFactory factory = new OnnxEmbeddingFactory();
        Model model = loadModel(modelId);
        return factory.createEmbedding(model, text);
    }

    private Model loadModel(String modelId) {
        return provider.getModel(modelId);
    }

}