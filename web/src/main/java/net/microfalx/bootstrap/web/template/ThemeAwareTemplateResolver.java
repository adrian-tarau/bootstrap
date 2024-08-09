package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.application.Theme;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A view resolver which is aware of the application template.
 */
public class ThemeAwareTemplateResolver extends SpringResourceTemplateResolver {

    private ApplicationService applicationService;
    private ApplicationContext applicationContext;

    private Map<String, Boolean> themedResources = new ConcurrentHashMap<>();

    public ThemeAwareTemplateResolver(ApplicationService applicationService) {
        this.applicationService = applicationService;
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
        return super.computeResourceName(configuration, ownerTemplate, template, prefix, suffix, forceSuffix,
                templateAliases, templateResolutionAttributes);
    }
}
