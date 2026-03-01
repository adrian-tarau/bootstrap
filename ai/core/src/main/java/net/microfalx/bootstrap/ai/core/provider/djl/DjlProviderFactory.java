package net.microfalx.bootstrap.ai.core.provider.djl;

import net.microfalx.bootstrap.ai.api.Provider;
import net.microfalx.bootstrap.ai.core.AbstractProviderFactory;

@net.microfalx.lang.annotation.Provider
public class DjlProviderFactory extends AbstractProviderFactory {

    @Override
    public Provider createProvider() {
        net.microfalx.bootstrap.ai.api.Provider.Builder builder = new net.microfalx.bootstrap.ai.api.Provider.Builder("jlama");
        builder.name("DJL").description("An Engine-Agnostic Deep Learning Framework in Java");
        builder.version("0.36.0").author("DJL Team").license("Apache-2.0")
                .chatFactory(new DjlChatFactory());
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.bootstrap.ai.api.Provider.Builder builder) {

    }
}
