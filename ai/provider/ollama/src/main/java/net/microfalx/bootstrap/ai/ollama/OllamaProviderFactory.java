package net.microfalx.bootstrap.ai.ollama;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Provider;
import net.microfalx.bootstrap.ai.core.AbstractProviderFactory;
import net.microfalx.lang.UriUtils;

import static net.microfalx.lang.StringUtils.isNotEmpty;

@net.microfalx.lang.annotation.Provider
@Setter
@Getter
public class OllamaProviderFactory extends AbstractProviderFactory {

    @Override
    public Provider createProvider() {
        net.microfalx.bootstrap.ai.api.Provider.Builder builder = new net.microfalx.bootstrap.ai.api.Provider.Builder("ollama");
        if (isNotEmpty(getProperties().getOllamaUri())) {
            builder.uri(UriUtils.parseUri(getProperties().getOllamaUri()), getProperties().getOllamaApiKey());
        }
        builder.name("Ollama").description("A locally deployed AI model runner");
        builder.version("0.7.0").author("Ollama Team").license("MIT")
                .chatFactory(new OllamaChatFactory()).tag("ollama");
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.bootstrap.ai.api.Provider.Builder builder) {

        // Google Gemma
        builder.model((Model.Builder) Model.create("Gemma 3 (1b)", "gemma3:1b")
                .maximumContextLength(32_000).tag("google").tag("gemma"));
        builder.model((Model.Builder) Model.create("Gemma 3 (4b)", "gemma3:4b")
                .maximumContextLength(128_000).tag("google").tag("gemma"));
        builder.model((Model.Builder) Model.create("Gemma 3 (12b)", "gemma3:12b")
                .maximumContextLength(128_000).tag("google").tag("gemma"));
        builder.model((Model.Builder) Model.create("Gemma 3 (27b)", "gemma3:27b")
                .maximumContextLength(128_000).tag("google").tag("gemma"));

        // AlliBaba Qwen
        builder.model((Model.Builder) Model.create("Qwen3 (0.6b)", "qwen3:0.6b")
                .maximumContextLength(40_000)
                .canThink().hasTools().tag("alibaba").tag("qwen"));
        builder.model((Model.Builder) Model.create("Qwen3 (1.7b)", "qwen3:1.7b")
                .maximumContextLength(40_000).canThink().hasTools().tag("alibaba").tag("qwen"));
        builder.model((Model.Builder) Model.create("Qwen3 (4b)", "qwen3:4b")
                .maximumContextLength(256_000).canThink().hasTools().tag("alibaba").tag("qwen"));
        builder.model((Model.Builder) Model.create("Qwen3 (8b)", "qwen3:8b")
                .maximumContextLength(40_000).canThink().hasTools().tag("alibaba").tag("qwen"));
        builder.model((Model.Builder) Model.create("Qwen3 (14b)", "qwen3:14b")
                .maximumContextLength(40_000).canThink().hasTools().tag("alibaba").tag("qwen"));
        builder.model((Model.Builder) Model.create("Qwen3 (30b)", "qwen3:30b")
                .maximumContextLength(256_000).canThink().hasTools().tag("alibaba").tag("qwen"));

        // DeepSeek R1
        builder.model((Model.Builder) Model.create("DeepSeek-R1 (1.5b)", "deepseek-r1:1.5b")
                .maximumContextLength(128_000).canThink().tag("deepseek"));
        builder.model((Model.Builder) Model.create("DeepSeek-R1 (7b)", "deepseek-r1:7b")
                .maximumContextLength(128_000).canThink().tag("deepseek"));
        builder.model((Model.Builder) Model.create("DeepSeek-R1 (8b)", "deepseek-r1:8b")
                .maximumContextLength(128_000).canThink().tag("deepseek"));
        builder.model((Model.Builder) Model.create("DeepSeek-R1 (14b)", "deepseek-r1:14b")
                .maximumContextLength(128_000).canThink().tag("deepseek"));
        builder.model((Model.Builder) Model.create("DeepSeek-R1 (32b)", "deepseek-r1:32b")
                .maximumContextLength(128_000).canThink().tag("deepseek"));

        // Meta Llama 3
        builder.model((Model.Builder) Model.create("Llama 3.1 (8b)", "llama3.1:8b")
                .maximumContextLength(128_000).hasTools().tag("meta").tag("llama"));
        builder.model((Model.Builder) Model.create("Llama 3.1 (70b)", "llama3.1:70b")
                .maximumContextLength(128_000).hasTools().tag("meta").tag("llama"));
        builder.model((Model.Builder) Model.create("Llama 3.2 (1b)", "llama3.2:1b")
                .maximumContextLength(128_000).hasTools().tag("meta").tag("llama"));
        builder.model((Model.Builder) Model.create("Llama 3.2 (3b)", "llama3.2:3b")
                .maximumContextLength(128_000).hasTools().tag("meta").tag("llama"));
        builder.model((Model.Builder) Model.create("Llama 3.3 (70b)", "llama3.3:70b")
                .maximumContextLength(128_000).hasTools().tag("meta").tag("llama"));

        // Meta Llama 4
        builder.model((Model.Builder) Model.create("Llama 4 (16x17b)", "llama4:16x17b")
                .maximumContextLength(10_000_000).hasTools().tag("meta").tag("llama"));

    }
}
