package net.microfalx.bootstrap.restapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import net.microfalx.lang.annotation.Order;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static net.microfalx.lang.StringUtils.addEndSlash;
import static net.microfalx.lang.StringUtils.addStartSlash;

@Configuration
@PropertySource("classpath:swagger.properties")
public class RestApiConfiguration {

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui.html",
            "/swagger-ui/**"
    };

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

    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http, ApiCredentialService credentialService) throws Exception {
        String apiPathAll = addMatchAll("api");
        HttpSecurity httpSecurity = http.securityMatcher(apiPathAll);
        updateCsrf(httpSecurity);
        updateSecurity(httpSecurity, credentialService);
        updateExceptionHandling(httpSecurity);
        return http.build();
    }

    private Info getInfo() {
        return new Info().title(properties.getName()).version(properties.getVersion())
                .description(properties.getDescription());
    }

    private Components getComponents() {
        Components components = new Components();
        components.addSecuritySchemes("bearer", new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .scheme("bearer").bearerFormat("Opaque"));
        components.addSecuritySchemes("apiKey", new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .scheme("apiKey"));
        return components;
    }

    private String addMatchAll(String path) {
        String result = addStartSlash(addEndSlash(path));
        return result + "**";
    }

    private void updateCsrf(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
    }

    private void updateSecurity(HttpSecurity httpSecurity, ApiCredentialService credentialService) throws Exception {
        String apiPathAll = addMatchAll("api");
        httpSecurity.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(properties.getPathsToMatch()).authenticated()
                        .requestMatchers(apiPathAll).permitAll()
                );
        httpSecurity.addFilterBefore(new ApiKeyOrBearerFilter(credentialService),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
    }

    private void updateExceptionHandling(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.exceptionHandling(e -> e
                .authenticationEntryPoint(new RestApiAuthenticationEntryPoint())
                .accessDeniedHandler(new RestApiAccessDeniedHandler())
        );
    }

}
