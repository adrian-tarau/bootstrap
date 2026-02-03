package net.microfalx.bootstrap.feature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FeatureMvcConfiguration implements WebMvcConfigurer {

    @Autowired private FeatureService featureService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new FeatureHandlerInterceptor(featureService));
    }
}
