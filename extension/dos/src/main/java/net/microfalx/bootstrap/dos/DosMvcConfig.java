package net.microfalx.bootstrap.dos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DosMvcConfig implements WebMvcConfigurer {

    @Autowired private DosService dosService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DosHandlerInterceptor(dosService));
    }
}
