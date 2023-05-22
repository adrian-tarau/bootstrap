package net.microfalx.bootstrap.core.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.ConverterRegistry;

@Configuration
public class DateTimeConfig {

    @Autowired
    private ConverterRegistry converterRegistry;

    @PostConstruct
    public void conversionService() {
        // converterRegistry.addConverter(new IsoLocalDateTime());
    }
}
