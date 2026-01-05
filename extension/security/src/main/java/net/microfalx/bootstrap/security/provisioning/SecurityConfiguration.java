package net.microfalx.bootstrap.security.provisioning;

import net.microfalx.bootstrap.restapi.RestApiAccessDeniedHandler;
import net.microfalx.bootstrap.restapi.RestApiAuthenticationEntryPoint;
import net.microfalx.bootstrap.security.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;

import static net.microfalx.lang.StringUtils.addEndSlash;
import static net.microfalx.lang.StringUtils.addStartSlash;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private OAuth2Properties oauth2Properties;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private UserService userService;

    @Bean
    @Order(5)
    public SecurityFilterChain healthChain(HttpSecurity httpSecurity, RememberMeServices rememberMeServices) throws Exception {
        httpSecurity.securityMatcher(EndpointRequest.to(HealthEndpoint.class));
        return httpSecurity.build();
    }

    @Bean
    @Order(10)
    public SecurityFilterChain webChain(HttpSecurity httpSecurity, RememberMeServices rememberMeServices) throws Exception {
        if (securityProperties.isEnabled()) {
            httpSecurity = httpSecurity.securityMatcher(addMatchAll("/"));
            // everything else requires authentication
            httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            updateLogin(httpSecurity);
            updateOAuth2(httpSecurity);
            updateRememberMe(httpSecurity, rememberMeServices);
            updateCommon(httpSecurity);
            updateExceptionHandling(httpSecurity);
            return httpSecurity.build();
        } else {
            httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            updateCommon(httpSecurity);
            return httpSecurity.build();
        }
    }

    private void updateCommon(HttpSecurity httpSecurity) throws Exception {
        updateSessionManagement(httpSecurity);
        updateOther(httpSecurity);
        updateHeaders(httpSecurity);
        updateAnonymous(httpSecurity);
    }

    private void updateRememberMe(HttpSecurity httpSecurity, RememberMeServices rememberMeServices) throws Exception {
        httpSecurity.rememberMe(rememberMe -> rememberMe.rememberMeServices(rememberMeServices));
    }

    private void updateSessionManagement(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
    }

    private void updateOAuth2(HttpSecurity httpSecurity) throws Exception {
        if (oauth2Properties.isEnabled()) {
            httpSecurity.oauth2Login(oauth2 -> oauth2.loginPage("/login")
                    .userInfoEndpoint(userInfo -> userInfo
                            .oidcUserService(new OidcUserService())
                            .userService(new OAuth2UserService())
                    ));
        }
    }

    private void updateOther(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(Customizer.withDefaults());
    }

    private void updateHeaders(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.headers(headers -> {
            //headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
            //headers.xssProtection(Customizer.withDefaults());
            //headers.referrerPolicy(rp -> rp.policy(NO_REFERRER));
        });
    }

    private void updateAnonymous(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.anonymous(a -> a.principal("guest"));
    }

    private void updateExceptionHandling(HttpSecurity httpSecurity) throws Exception {
        var jsonEntryPoint = new RestApiAuthenticationEntryPoint();
        var jsonDenied = new RestApiAccessDeniedHandler();
        // Match requests that accept application/json
        var contentNegotiation = httpSecurity.getSharedObject(org.springframework.web.accept.ContentNegotiationStrategy.class);
        var jsonMatcher = new org.springframework.security.web.util.matcher.MediaTypeRequestMatcher(
                contentNegotiation, org.springframework.http.MediaType.APPLICATION_JSON);
        jsonMatcher.setIgnoredMediaTypes(java.util.Set.of(org.springframework.http.MediaType.ALL));
        // For JSON-accepting requests, send JSON 401/403 instead of redirect
        httpSecurity.exceptionHandling(e -> e
                .defaultAuthenticationEntryPointFor(jsonEntryPoint, jsonMatcher)
                .defaultAccessDeniedHandlerFor(jsonDenied, jsonMatcher)
        );
    }


    private void updateLogin(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.formLogin(login -> login.loginPage("/login").loginProcessingUrl("/login/auth")
                .usernameParameter("username").passwordParameter("password").permitAll());
        httpSecurity.logout(logout -> logout.clearAuthentication(true)
                .invalidateHttpSession(true).logoutUrl("/logout")
                .logoutSuccessUrl("/"));
    }

    private void allowPath(HttpSecurity httpSecurity, String path) throws Exception {
        httpSecurity.authorizeHttpRequests(auth -> auth.requestMatchers(addMatchAll(path)).permitAll());
    }

    private String addMatchAll(String path) {
        String result = addStartSlash(addEndSlash(path));
        return result + "**";
    }


}
