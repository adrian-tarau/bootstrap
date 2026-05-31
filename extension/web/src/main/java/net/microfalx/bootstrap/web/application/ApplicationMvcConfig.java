package net.microfalx.bootstrap.web.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApplicationMvcConfig implements WebMvcConfigurer {

    @Autowired private ApplicationService applicationService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApplicationRequestInterceptor(applicationService, applicationService.getApplication()));
    }

}
