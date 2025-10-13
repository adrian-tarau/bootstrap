package net.microfalx.bootstrap.restapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:swagger.properties")
public class RestApiConfig {

    @Autowired private RestApiProperties properties;

    @Bean
    public OpenAPI appOpenAPI() {
        OpenAPI api = new OpenAPI().info(getInfo());
        api.components(getComponents());
        return api;
    }

    @Bean
    public GroupedOpenApi publicApi() {
        GroupedOpenApi.Builder builder = GroupedOpenApi.builder().group(properties.getVersion())
                .packagesToScan(properties.getPackagesToScan()).pathsToMatch(properties.getPathsToMatch())
                .displayName(properties.getName() + " " + properties.getVersion());
        return builder.build();
    }

    private Info getInfo() {
        return new Info().title(properties.getName()).version(properties.getVersion()).description(properties.getDescription());
    }

    private Components getComponents() {
        Components components = new Components();
        components.addSecuritySchemes("bearer", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));
        components.addSecuritySchemes("basicAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"));
        components.addSecuritySchemes("apiKeyAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("apiKey"));
        return components;
    }

}
