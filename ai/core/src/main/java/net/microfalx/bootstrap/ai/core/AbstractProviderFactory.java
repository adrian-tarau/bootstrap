package net.microfalx.bootstrap.ai.core;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.ai.api.Provider;

/**
 * Base class for all provider factories.
 */
public abstract class AbstractProviderFactory implements Provider.Factory {

    /**
     * Properties to be used with the provider factory.
     */
    @Getter
    @Setter
    private AiProperties properties = new AiProperties();
}
