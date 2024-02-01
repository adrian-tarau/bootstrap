package net.microfalx.bootstrap.security.provisioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SecuritySettings settings;

    @Bean
    public UserDetailsManager createUserDetailsManager() {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        manager.setUserCache(new SpringCacheBasedUserCache(new ConcurrentMapCache("security_user")));
        manager.setEnableAuthorities(true);
        manager.setEnableGroups(true);
        updateUserInfoManager(manager);
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (settings.isEnabled()) {
            http.authorizeHttpRequests(auth -> auth.requestMatchers("/asset/**").permitAll());
            http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            http.formLogin(login -> login.loginPage("/auth").loginProcessingUrl("/auth/login")
                    .usernameParameter("username").passwordParameter("password").permitAll());
            http.logout(logout -> logout.clearAuthentication(true).invalidateHttpSession(true).logoutUrl("/auth/logout").permitAll());
            updateCsrv(http);
            updateHeaders(http);
            updateAnonymous(http);
            return http.build();
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            updateCsrv(http);
            updateHeaders(http);
            updateAnonymous(http);
            return http.build();
        }
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

    private void updateUserInfoManager(JdbcUserDetailsManager manager) {
    }
}
