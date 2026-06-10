package net.microfalx.bootstrap.core.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

@Configuration
public class ConverterConfiguration {

    @Bean
    @ConditionalOnMissingBean(ConversionService.class)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnNotWebApplication
    public ConversionService getConversionService() {
        return new DefaultFormattingConversionService();
    }

}
