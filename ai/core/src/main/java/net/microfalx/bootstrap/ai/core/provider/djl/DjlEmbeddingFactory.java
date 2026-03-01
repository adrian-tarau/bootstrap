package net.microfalx.bootstrap.ai.core.provider.djl;

import net.microfalx.bootstrap.ai.api.AiException;
import net.microfalx.bootstrap.ai.api.Embedding;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.core.AbstractEmbeddingFactory;

public class DjlEmbeddingFactory extends AbstractEmbeddingFactory {

    @Override
    public Embedding createEmbedding(Model model, String text) {
        throw new AiException("ONNX provider does not support chat models yet");
    }
}
