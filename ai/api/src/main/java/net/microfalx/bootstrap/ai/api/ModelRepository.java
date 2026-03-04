package net.microfalx.bootstrap.ai.api;

import net.microfalx.resource.Resource;

import java.io.IOException;

/**
 * A repository for downloading AI models.
 */
public interface ModelRepository {

    /**
     * Returns whether the repository supports the given model.
     *
     * @param model the model to check
     * @return {@code true} if the repository supports the model, {@code false} otherwise
     */
    boolean supports(Model model);

    /**
     * Resolves a model to a resource.
     *
     * @param model the model to resolve
     * @return a non-null instance, null if the model cannot be resolved by this repository
     */
    Resource resolve(Model model) throws IOException;
}
