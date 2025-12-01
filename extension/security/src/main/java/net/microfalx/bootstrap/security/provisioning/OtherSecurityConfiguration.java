package net.microfalx.bootstrap.security.provisioning;

import jakarta.servlet.Filter;
import jakarta.servlet.SessionCookieConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.sql.DataSource;

import static org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256;

@Configuration
public class OtherSecurityConfiguration {

    private static final String REMEMBER_ME_KEY = "remember-me";

    @Autowired
    private DataSource dataSource;

    @Bean
    public RememberMeServices createRememberMeServices(UserDetailsService userDetailsService) {
        TokenBasedRememberMeServices.RememberMeTokenAlgorithm encodingAlgorithm = SHA256;
        TokenBasedRememberMeServices rememberMe = new TokenBasedRememberMeServices(REMEMBER_ME_KEY, userDetailsService, encodingAlgorithm);
        rememberMe.setMatchingAlgorithm(encodingAlgorithm);
        return rememberMe;
    }

    @Bean
    public UserDetailsManager createUserDetailsManager() {
        SecurityUserDetailsManager manager = new SecurityUserDetailsManager(dataSource);
        manager.setUserCache(new SpringCacheBasedUserCache(new ConcurrentMapCache("security_user")));
        manager.setEnableAuthorities(false);
        manager.setEnableGroups(true);
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> sessionCookieCustomizer() {
        return factory -> factory.addInitializers(servletContext -> {
            SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
            sessionCookieConfig.setHttpOnly(true);
            sessionCookieConfig.setSecure(true);
        });
    }

    @Bean
    public Filter sameSiteCookieFilter() {
        return new SecurityFilter();
    }
}
