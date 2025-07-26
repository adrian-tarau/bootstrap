package net.microfalx.bootstrap.web.template;

import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.context.IContext;
import org.thymeleaf.extras.springsecurity6.util.SpringSecurityContextUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A class which wraps a security context and provides information about a user.
 */
public final class TemplateSecurityContext implements Authentication {

    private static final String ROLE_PREFIX = "ROLE_";

    private final Authentication authentication;
    private final Set<String> roles = new HashSet<>();

    public static TemplateSecurityContext get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof TemplateSecurityContext ? (TemplateSecurityContext) authentication : new TemplateSecurityContext(authentication);
    }

    public static TemplateSecurityContext get(IContext context) {
        ArgumentUtils.requireNonNull(context);
        return new TemplateSecurityContext(SpringSecurityContextUtils.getAuthenticationObject(context));
    }

    TemplateSecurityContext(Authentication authentication) {
        this.authentication = authentication;
        for (GrantedAuthority authority : getAuthorities()) {
            roles.add(normalizeRole(authority.getAuthority()));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authentication.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return authentication.getCredentials();
    }

    @Override
    public Object getDetails() {
        return authentication.getDetails();
    }

    @Override
    public Object getPrincipal() {
        return authentication.getPrincipal();
    }

    @Override
    public boolean isAuthenticated() {
        return authentication.isAuthenticated();
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        authentication.setAuthenticated(authenticated);
    }

    @Override
    public String getName() {
        return authentication.getName();
    }

    public boolean hasRoles(String... roles) {
        for (String role : roles) {
            if (this.roles.contains(StringUtils.toIdentifier(role))) return true;
        }
        return false;
    }

    public boolean hasRoles(Collection<String> roles) {
        for (String role : roles) {
            if (this.roles.contains(normalizeRole(role))) return true;
        }
        return false;
    }

    private String normalizeRole(String role) {
        role = role.toUpperCase();
        if (role.startsWith(ROLE_PREFIX)) role = role.substring(ROLE_PREFIX.length());
        return StringUtils.toIdentifier(role);
    }

    @Override
    public String toString() {
        return "SecurityContext{" +
                "name=" + authentication.getName() +
                ", roles=" + roles +
                ", authentication=" + authentication +
                '}';
    }
}
