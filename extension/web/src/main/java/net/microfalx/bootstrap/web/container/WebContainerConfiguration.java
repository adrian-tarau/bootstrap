package net.microfalx.bootstrap.web.container;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class WebContainerConfiguration {

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> bean =
                new FilterRegistrationBean<>(new ForwardedHeaderFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyThreadPoolCustomizer() {
        return factory -> factory.addServerCustomizers(server -> {
            QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            if (threadPool != null) {
                threadPool.setName("Boot-Web");
            }
        });
    }
}
