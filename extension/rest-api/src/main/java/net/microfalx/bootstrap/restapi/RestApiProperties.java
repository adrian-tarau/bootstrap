package net.microfalx.bootstrap.restapi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.rest.api")
@Getter
@Setter
public class RestApiProperties {

    private String uiPath = "/api/docs";
    private boolean uiEnabled = true;

    private String specsPath = "/api/specs";
    private boolean specEnabled = true;

    @Value("${bootstrap.application.name}")
    private String name = "Bootstrap";
    @Value("${bootstrap.application.description}")
    private String description = "";
    private String version = "v1";

    private String packagesToScan = "net.microfalx.bootstrap";
    private String pathsToMatch = "/api/v*/**";
}
