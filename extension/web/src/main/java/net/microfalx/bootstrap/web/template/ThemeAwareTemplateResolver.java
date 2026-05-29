package net.microfalx.bootstrap.web.template;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.application.Theme;
import org.slf4j.event.Level;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;
import static net.microfalx.resource.ResourceUtils.CLASS_PATH_SCHEME;

/**
 * A view resolver which is aware of the application template.
 */
@Slf4j
public class ThemeAwareTemplateResolver extends SpringResourceTemplateResolver {

    private final ApplicationService applicationService;
    private final ResourceService resourceService;
    private ApplicationContext applicationContext;

    private final Map<String, Boolean> themedResources = new ConcurrentHashMap<>();
    private final Set<String> resolvedTemplates = new CopyOnWriteArraySet<>();

    public ThemeAwareTemplateResolver(ApplicationService applicationService, ResourceService resourceService) {
        requireNonNull(applicationService);
        requireNonNull(resourceService);
        this.applicationService = applicationService;
        this.resourceService = resourceService;
        setOrder(Integer.MIN_VALUE);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        super.setApplicationContext(applicationContext);
        this.applicationContext = applicationContext;
    }

    @Override
    protected String computeResourceName(IEngineConfiguration configuration, String ownerTemplate, String template,
                                         String prefix, String suffix, boolean forceSuffix, Map<String, String> templateAliases,
                                         Map<String, Object> templateResolutionAttributes) {
        Theme currentTheme = applicationService.getCurrentTheme();
        String suffixWithTheme = "_" + currentTheme.getId() + suffix;
        Boolean hasThemedTemplate = themedResources.get(template);
        if (hasThemedTemplate == null) {
            String resourceUri = super.computeResourceName(configuration, ownerTemplate, template, prefix, suffixWithTheme, forceSuffix,
                    templateAliases, templateResolutionAttributes);
            Resource resource = applicationContext.getResource(resourceUri);
            hasThemedTemplate = resource.exists();
            themedResources.put(template, hasThemedTemplate);
        }
        if (hasThemedTemplate) suffix = suffixWithTheme;
        String resourceUri = super.computeResourceName(configuration, ownerTemplate, template, prefix, suffix, forceSuffix,
                templateAliases, templateResolutionAttributes);
        if (resolvedTemplates.add(resourceUri)) {
            boolean exists = applicationContext.getResource(resourceUri).exists();
            org.slf4j.event.Level level = exists ? Level.DEBUG : Level.ERROR;
            if (ownerTemplate != null) {
                LOGGER.atLevel(level).log("Resolve template '{}' (owner template '{}') with themed resource URI '{}', themed {}, exists {}",
                        template, ownerTemplate, resourceUri, hasThemedTemplate, exists);
            } else {
                LOGGER.atLevel(level).log("Resolve template '{}' with themed resource URI '{}', themed {}, exists {}",
                        template, resourceUri, hasThemedTemplate, exists);
            }
        }
        if (resourceUri.startsWith(CLASS_PATH_SCHEME)) {
            URI uri = null;
            try {
                uri = URI.create(resourceUri);
            } catch (Exception e) {
                // if we cannot parse, we ignore
            }
            try {
                uri = resourceService.resolveUri(uri);
                if (uri != null) resourceUri = uri.toASCIIString();
            } catch (Exception e) {
                // any exception here, just ignore and use the original
                LOGGER.warn("Failed to resolve URI '{}', root cause: {}", uri, getRootCauseDescription(e));
            }
        }
        return resourceUri;
    }
}
