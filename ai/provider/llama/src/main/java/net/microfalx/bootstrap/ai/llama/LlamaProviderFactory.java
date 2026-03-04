package net.microfalx.bootstrap.ai.llama;

import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Provider;
import net.microfalx.bootstrap.ai.core.AbstractProviderFactory;

@net.microfalx.lang.annotation.Provider
public class LlamaProviderFactory extends AbstractProviderFactory {

    @Override
    public Provider createProvider() {
        net.microfalx.bootstrap.ai.api.Provider.Builder builder = new net.microfalx.bootstrap.ai.api.Provider.Builder("llama");
        builder.name("Llama").description("An embeddable LLM inference");
        builder.version("b8189").author("Llama Team").license("MIT")
                .chatFactory(new LlamaChatFactory()).tag("llama");
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.bootstrap.ai.api.Provider.Builder builder) {
        // AlliBaba Qwen
        builder.model((Model.Builder) Model.create("qwen2_5_0_5b",
                        "Qwen2.5 (0.5b)", "qwen2.5:0.5b").maximumContextLength(32_000)
                .downloadUri("https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true")
                .canThink().hasTools().asDefault().tag("alibaba").tag("qwen")
        );
        builder.model((Model.Builder) Model.create("qwen3_1_7b",
                        "Qwen2.5 (1.5b)", "qwen2.5:1.5b").maximumContextLength(32_000)
                .downloadUri("https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf?download=true")
                .canThink().hasTools().tag("alibaba").tag("qwen"));

    }
}
