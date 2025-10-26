package net.microfalx.bootstrap.security.provisioning;

import net.microfalx.bootstrap.restapi.RestApiAccessDeniedHandler;
import net.microfalx.bootstrap.restapi.RestApiAuthenticationEntryPoint;
import net.microfalx.lang.annotation.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.sql.DataSource;

import static net.microfalx.lang.StringUtils.addEndSlash;
import static net.microfalx.lang.StringUtils.addStartSlash;
import static org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String REMEMBER_ME_KEY = "remember-me";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SecurityProperties settings;

    @Bean
    public UserDetailsManager createUserDetailsManager() {
        SecurityUserDetailsManager manager = new SecurityUserDetailsManager(dataSource);
        manager.setUserCache(new SpringCacheBasedUserCache(new ConcurrentMapCache("security_user")));
        manager.setEnableAuthorities(false);
        manager.setEnableGroups(true);
        return manager;
    }

    @Bean
    public RememberMeServices createRememberMeServices(UserDetailsService userDetailsService) {
        TokenBasedRememberMeServices.RememberMeTokenAlgorithm encodingAlgorithm = SHA256;
        TokenBasedRememberMeServices rememberMe = new TokenBasedRememberMeServices(REMEMBER_ME_KEY, userDetailsService, encodingAlgorithm);
        rememberMe.setMatchingAlgorithm(encodingAlgorithm);
        return rememberMe;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @Order(10)
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, RememberMeServices rememberMeServices) throws Exception {
        if (settings.isEnabled()) {
            allowStandardPaths(httpSecurity);
            updateLogin(httpSecurity);
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
        updateCsrf(httpSecurity);
        updateHeaders(httpSecurity);
        updateAnonymous(httpSecurity);
    }

    private void updateRememberMe(HttpSecurity httpSecurity, RememberMeServices rememberMeServices) throws Exception {
        httpSecurity.rememberMe(rememberMe -> rememberMe.rememberMeServices(rememberMeServices));
    }

    private void updateCsrf(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
    }

    private void updateHeaders(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
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

    private void allowStandardPaths(HttpSecurity httpSecurity) throws Exception {
        allowPath(httpSecurity, "asset");
        allowPath(httpSecurity, "css");
        allowPath(httpSecurity, "js");
        allowPath(httpSecurity, "image");
        allowPath(httpSecurity, "favicon.ico");
        allowPath(httpSecurity, "font");
        allowPath(httpSecurity, "login");
        allowPath(httpSecurity, "settings/session");
        allowPath(httpSecurity, "ping");
        allowPath(httpSecurity, "api");
        configureMetrics(httpSecurity);
        httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
    }

    private void updateLogin(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.formLogin(login -> login.loginPage("/login").loginProcessingUrl("/login/auth")
                //.successForwardUrl("/").failureForwardUrl("/login?error")
                .usernameParameter("username").passwordParameter("password").permitAll());
        httpSecurity.logout(logout -> logout.clearAuthentication(true).invalidateHttpSession(true).logoutUrl("/logout").permitAll());
    }

    private void configureMetrics(HttpSecurity http) throws Exception {
        //http.securityMatcher(EndpointRequest.to(HealthEndpoint.class));
    }

    private void allowPath(HttpSecurity http, String path) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(addEndSlash(addStartSlash(path)) + "**").permitAll());
    }


}
