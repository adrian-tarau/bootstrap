package net.microfalx.bootstrap.security.provisioning;

import net.microfalx.bootstrap.security.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static net.microfalx.lang.StringUtils.addEndSlash;
import static net.microfalx.lang.StringUtils.addStartSlash;

@Configuration
public class OAuth2SecurityConfiguration {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private OAuth2Properties oauth2Properties;

    @Autowired
    private UserService userService;

    @Bean
    @Order(9)
    public SecurityFilterChain oauthChain(HttpSecurity httpSecurity) throws Exception {
        if (securityProperties.isEnabled() && oauth2Properties.isEnabled()) {
            httpSecurity.securityMatcher(addMatchAll("/oauth2"), addMatchAll("/login/oauth2"));
            httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            updateOAuth2(httpSecurity);
            return httpSecurity.build();
        } else {
            httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return httpSecurity.build();
        }
    }

    private void updateOAuth2(HttpSecurity httpSecurity) throws Exception {
        if (oauth2Properties.isEnabled()) {
            httpSecurity.oauth2Login(oauth2 -> oauth2.loginPage("/login")
                    .userInfoEndpoint(userInfo -> userInfo
                            .oidcUserService(new OidcUserService(userService, oauth2Properties))
                            .userService(new OAuth2UserService(userService, oauth2Properties))
                    ));
        }
    }

    private String addMatchAll(String path) {
        String result = addStartSlash(addEndSlash(path));
        return result + "**";
    }
}
