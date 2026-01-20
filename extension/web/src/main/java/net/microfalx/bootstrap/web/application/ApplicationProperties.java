package net.microfalx.bootstrap.web.application;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

@Configuration
@ConfigurationProperties("bootstrap.application")
@Getter
@Setter
@ToString
public class ApplicationProperties {

    private String name = "Default";

    private String description = "The default application descriptor";

    private String owner;

    private String logo = "default.png";

    private String url = "#";

    private String theme = Theme.DEFAULT;

    private Map<String, String> domainThemes= Collections.emptyMap();

    private String systemTheme;

    private String version = "1.0.0";

    private String timeZone;

}
