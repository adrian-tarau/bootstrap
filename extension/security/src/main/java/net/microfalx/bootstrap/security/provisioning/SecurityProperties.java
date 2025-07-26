package net.microfalx.bootstrap.security.provisioning;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.security")
@Setter
@Getter
@ToString
public class SecurityProperties {

    private boolean enabled = false;
    private boolean social = false;
    private boolean register = false;
    private boolean terms = false;

    private String loginMessage = "Sign in to start your session";

    private String adminUserName = "admin";
    private String adminPassword = "WhfAeDkf8857";
    private String guestUserName = "guest";
    private String guestPassword = "ZWqHAE7at542";
}
