package net.microfalx.bootstrap.web.application;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.application")
public class ApplicationProperties {

    @NotBlank
    private String name = "Default";

    @NotBlank
    private String description = "The default application descriptor";

    @NotBlank
    private String theme = "default";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
