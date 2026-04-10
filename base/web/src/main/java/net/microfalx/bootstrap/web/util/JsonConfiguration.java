package net.microfalx.bootstrap.web.util;

import net.microfalx.bootstrap.core.utils.JsonSerde;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JsonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.serializerByType(LocalDateTime.class, new JsonSerde.SystemZoneLocalDateTimeSerializer());
            builder.deserializerByType(LocalDateTime.class, new JsonSerde.SystemZoneLocalDateTimeDeserializer());
        };
    }
}
