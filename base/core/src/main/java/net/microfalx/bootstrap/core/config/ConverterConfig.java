package net.microfalx.bootstrap.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

@Configuration
public class ConverterConfig {

    @Bean
    @ConditionalOnMissingBean(ConversionService.class)
    @ConditionalOnNotWebApplication
    public ConversionService getConversionService() {
        return new DefaultFormattingConversionService();
    }

}
