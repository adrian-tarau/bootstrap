package net.microfalx.bootstrap.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("boostrap.mail")
@Getter
@Setter
public class MailProperties {

    private String host = "localhost";
    private int port = 25;
    private boolean tls;
    private String userName;
    private String password;

    private String from = "admin@localhost";
}
