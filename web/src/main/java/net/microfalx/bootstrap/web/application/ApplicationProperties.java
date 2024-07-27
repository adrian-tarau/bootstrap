package net.microfalx.bootstrap.web.application;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.application")
@Getter
@Setter
@ToString
public class ApplicationProperties {

    public static final String DEFAULT_THEME = "adminator";

    @NotBlank
    private String name = "Default";

    @NotBlank
    private String description = "The default application descriptor";

    @NotBlank
    private String owner;

    @NotBlank
    private String logo = "default.png";

    @NotBlank
    private String url = "#";

    @NotBlank
    private String theme = "adminator";

    @NotBlank
    private String version = "1.0.0";

    private String timeZone;

}
