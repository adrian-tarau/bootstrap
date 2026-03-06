package net.microfalx.bootstrap.ai.openai;

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
public class OpenAiProviderFactory extends AbstractProviderFactory {

    @Override
    public Provider createProvider() {
        net.microfalx.bootstrap.ai.api.Provider.Builder builder = new net.microfalx.bootstrap.ai.api.Provider.Builder("openai");
        if (isNotEmpty(getProperties().getOpenAiUri())) {
            builder.uri(UriUtils.parseUri(getProperties().getOpenAiUri()), getProperties().getOpenAiApiKey());
        }
        builder.name("OpenAI").description("Develops safe and beneficial artificial general " +
                "intelligence, which it defines as highly autonomous systems that outperform humans at most economically valuable work.");
        builder.version("4.1").author("OpenAI Team").license("Proprietary")
                .chatFactory(new OpenAiChatFactory()).tag("openai");
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.bootstrap.ai.api.Provider.Builder builder) {
        builder.model((Model.Builder) Model.create("O4 Mini", "o4-mini")
                .maximumContextLength(128_000).tag("openai").tag("mini"));
        builder.model((Model.Builder) Model.create("O3", "o3")
                .maximumContextLength(128_000).tag("openai").tag("o3"));
        builder.model((Model.Builder) Model.create("O3 Mini", "o3-mini")
                .maximumContextLength(128_000).tag("openai").tag("mini"));
        builder.model((Model.Builder) Model.create("O1", "o1")
                .maximumContextLength(128_000).tag("openai").tag("o1"));
        builder.model((Model.Builder) Model.create("GPT 4.1", "gpt-4.1")
                .maximumContextLength(128_000).tag("openai").tag("gpt"));
        builder.model((Model.Builder) Model.create("GPT 4o", "gpt-4o")
                .maximumContextLength(128_000).tag("openai").tag("gpt"));
        builder.model((Model.Builder) Model.create("GPT 4.1 Mini", "gpt-4.1-mini")
                .maximumContextLength(128_000).tag("openai").tag("gpt").tag("mini"));
        builder.model((Model.Builder) Model.create("GPT 4o Mini", "gpt-4o-mini")
                .maximumContextLength(128_000).tag("openai").tag("gpt").tag("mini"));
        builder.model((Model.Builder) Model.create("GPT 4.1 Nano", "gpt-4.1-nano")
                .maximumContextLength(128_000).tag("openai").tag("gpt").tag("nano"));
    }
}
