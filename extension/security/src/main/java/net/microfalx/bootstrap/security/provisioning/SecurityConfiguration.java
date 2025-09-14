package net.microfalx.bootstrap.security.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class SecurityConfiguration {

    private static final String REMEMBER_ME_KEY = "rememberme";

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
    public SecurityFilterChain filterChain(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {
        if (settings.isEnabled()) {
            allowStandardPaths(http);
            configureLogin(http);
            updateRememberMe(http, rememberMeServices);
            updateCommon(http);
            return http.build();
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            updateCommon(http);
            return http.build();
        }
    }

    private void updateCommon(HttpSecurity http) throws Exception {
        updateCsrv(http);
        updateHeaders(http);
        updateAnonymous(http);
    }

    private void updateRememberMe(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {
        http.rememberMe(rememberMe -> rememberMe.rememberMeServices(rememberMeServices));
    }

    private void updateCsrv(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
    }

    private void updateHeaders(HttpSecurity http) throws Exception {
        http.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
    }

    private void updateAnonymous(HttpSecurity http) throws Exception {
        http.anonymous(a -> a.principal("guest"));
    }

    private void allowStandardPaths(HttpSecurity http) throws Exception {
        allowPath(http, "asset");
        allowPath(http, "css");
        allowPath(http, "js");
        allowPath(http, "image");
        allowPath(http, "font");
        allowPath(http, "login");
        allowPath(http, "settings/session");
        allowPath(http, "ping");
        configureMetrics(http);
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
    }

    private void configureLogin(HttpSecurity http) throws Exception {
        http.formLogin(login -> login.loginPage("/login").loginProcessingUrl("/login/auth")
                //.successForwardUrl("/").failureForwardUrl("/login?error")
                .usernameParameter("username").passwordParameter("password").permitAll());
        http.logout(logout -> logout.clearAuthentication(true).invalidateHttpSession(true).logoutUrl("/logout").permitAll());
    }

    private void configureMetrics(HttpSecurity http) throws Exception {
        //http.securityMatcher(EndpointRequest.to(HealthEndpoint.class));
    }

    private void allowPath(HttpSecurity http, String path) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(addEndSlash(addStartSlash(path)) + "**").permitAll());
    }


}
