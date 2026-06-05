package net.microfalx.bootstrap.web.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

@Configuration("WebApplicationProperties")
@ConfigurationProperties("bootstrap.application")
@Getter
@Setter
@ToString(callSuper = true)
public class ApplicationProperties extends net.microfalx.bootstrap.application.ApplicationProperties {

    private String logo = "default.png";

    private String theme = Theme.DEFAULT;

    private Map<String, String> domainThemes = Collections.emptyMap();

    private String systemTheme;

    private Theme.Mode themeMode = Theme.Mode.LIGHT;

}
