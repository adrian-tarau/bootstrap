package net.microfalx.bootstrap.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("bootstrap.mail")
@Getter
@Setter
public class MailProperties {

    static final String DEFAULT_FROM = "admin@localhost";

    private String host = "localhost";
    private int port = 25;
    private boolean tls;
    private String userName;
    private String password;

    private String from = DEFAULT_FROM;

    private int maximumRetryCount = 10;
    private Duration retention = Duration.ofDays(7);
    private Duration retryInterval = Duration.ofSeconds(60);
}
