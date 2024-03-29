package net.microfalx.bootstrap.web.application;

import jakarta.validation.constraints.NotBlank;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.application")
@ToString
public class ApplicationProperties {

    public static final String DEFAULT_THEME = "adminator";

    @NotBlank
    private String name = "Default";

    @NotBlank
    private String description = "The default application descriptor";

    @NotBlank
    private String logo = "default.png";

    @NotBlank
    private String theme = "adminator";

    @NotBlank
    private String version = "1.0.0";

    private String timeZone;

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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return "ApplicationProperties{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", theme='" + theme + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
