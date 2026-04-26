package net.microfalx.bootstrap.configuration.annotation;

import net.microfalx.bootstrap.configuration.ConfigurationPropertiesRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation to enable {@link net.microfalx.bootstrap.configuration.Configuration} proxies
 * for an easier access to application configuration.
 * <p>
 * Once enabled, interfaces annotated with {@link ConfigurationMapping} will create proxies
 * mapped over the configuration with a given prefix.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConfigurationPropertiesRegistrar.class)
public @interface EnableConfigurationMapping {

    /**
     * Alias for the {@link #basePackages()} attribute.
     *
     * @return the base packages to scan
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for configuration proxies. {@link #value()} is an alias for (and mutually
     * exclusive with) this attribute.
     * <p>
     *
     * @return the base packages to scan
     */
    @AliasFor("value")
    String[] basePackages() default {};
}
