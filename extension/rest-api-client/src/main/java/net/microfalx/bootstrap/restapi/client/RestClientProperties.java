package net.microfalx.bootstrap.restapi.client;

import lombok.Getter;
import lombok.Setter;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@Configuration
@ConfigurationProperties("bootstrap.rest.api.client")
@Getter
@Setter
public class RestClientProperties {

    private Duration connectTimeout = ofSeconds(5);
    private Duration readTimeout = ofSeconds(5);
    private Duration writeTimeout = ofSeconds(5);
    private Duration callTimeout = ofSeconds(30);
    private int threadCount = 10;
    private int maximumRetries = 3;
    private HttpLoggingInterceptor.Level loggingLevel = HttpLoggingInterceptor.Level.BODY;
}
