package net.microfalx.bootstrap.ai.core.provider.onnx;

import net.microfalx.bootstrap.ai.api.AiException;
import net.microfalx.bootstrap.ai.api.Embedding;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.core.AbstractEmbeddingFactory;
import net.microfalx.lang.JvmUtils;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.transformers.TransformersEmbeddingModel;

import java.util.List;
import java.util.Map;

public class OnnxEmbeddingFactory extends AbstractEmbeddingFactory {

    private EmbeddingModel embeddingModel;

    @Override
    public Embedding createEmbedding(Model model, String text) {
        EmbeddingResponse response = getEmbeddingModel().embedForResponse(List.of(text));
        return create(model, response.getResult());
    }

    private EmbeddingModel getEmbeddingModel() {
        if (embeddingModel != null) return embeddingModel;
        TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
        embeddingModel.setTokenizerResource("classpath:/onnx/all-MiniLM-L6-v2/tokenizer.json");
        embeddingModel.setModelResource("classpath:/onnx/all-MiniLM-L6-v2/model.onnx");
        embeddingModel.setResourceCacheDirectory(JvmUtils.getCacheDirectory("onnx").getAbsolutePath());
        embeddingModel.setTokenizerOptions(Map.of("padding", "true"));
        try {
            embeddingModel.afterPropertiesSet();
        } catch (Exception e) {
            throw new AiException("Failed to initialize embedding model", e);
        }
        this.embeddingModel = embeddingModel;
        return embeddingModel;
    }
}
