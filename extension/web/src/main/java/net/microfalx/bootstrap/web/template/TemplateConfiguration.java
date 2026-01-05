package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.web.application.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class TemplateConfiguration {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ThymeleafProperties properties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ITemplateResolver getViewResolver() {
        ThemeAwareTemplateResolver resolver = new ThemeAwareTemplateResolver(applicationService);
        resolver.setApplicationContext(this.applicationContext);
        resolver.setPrefix(this.properties.getPrefix());
        resolver.setSuffix(this.properties.getSuffix());
        resolver.setTemplateMode(this.properties.getMode());
        resolver.setOrder(-1);
        if (this.properties.getEncoding() != null) {
            resolver.setCharacterEncoding(this.properties.getEncoding().name());
        }
        resolver.setCacheable(this.properties.isCache());
        resolver.setCheckExistence(this.properties.isCheckTemplate());
        return resolver;
    }

}
