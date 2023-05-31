package net.microfalx.bootstrap.core.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DateTimeConfig {

    //@Autowired
    //private ConverterRegistry converterRegistry;

    @PostConstruct
    public void conversionService() {
        // converterRegistry.addConverter(new IsoLocalDateTime());
    }
}
