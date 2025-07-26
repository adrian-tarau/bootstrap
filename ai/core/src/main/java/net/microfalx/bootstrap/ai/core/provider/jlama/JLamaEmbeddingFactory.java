package net.microfalx.bootstrap.ai.core.provider.jlama;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.jlama.JlamaEmbeddingModel;
import net.microfalx.bootstrap.ai.api.Embedding;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.core.AbstractEmbeddingFactory;

public class JLamaEmbeddingFactory extends AbstractEmbeddingFactory {

    @Override
    public Embedding createEmbedding(Model model, String text) {
        EmbeddingModel embeddingModel = JlamaEmbeddingModel.builder()
                .modelName(model.getModelName())
                .build();
        return create(model, embeddingModel.embed(text).content());
    }

}
