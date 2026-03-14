package net.microfalx.bootstrap.model;

/**
 * A provider of mappers for mapping between models and other representations, such as DTOs, entities, etc.
 * <p>
 * The providers are registered when the {@link MetadataService} is initialized, and they are used to create mappers for the models. If
 * the provider implements {@link org.springframework.context.ApplicationContextAware} interface, the application context will be injected,
 * allowing the provider to access other beans and services.
 */
public interface MapperProvider {

    /**
     * Invokes during the initialization of the {@link MetadataService} to register mappers for models.
     */
    default void onInitializing() {
        // default implementation does nothing
    }

    /**
     * Invokes after the startup of the application to register mappers for models.
     */
    default void onStartup() {
        // default implementation does nothing
    }
}
