package net.microfalx.bootstrap.restapi.client;

import lombok.Getter;
import lombok.Setter;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

@Configuration
@ConfigurationProperties("bootstrap.rest.api.client")
@Getter
@Setter
public class RestClientProperties {

    private Duration connectTimeout = ofSeconds(10);
    private Duration readTimeout = ofSeconds(30);
    private Duration writeTimeout = ofSeconds(30);
    private Duration callTimeout = ofSeconds(30);
    private Duration reloadInterval = ofMinutes(5);
    private int threadCount = 10;
    private int maximumRetries = 3;
    private HttpLoggingInterceptor.Level loggingLevel = HttpLoggingInterceptor.Level.BASIC;
}
