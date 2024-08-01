package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.web.component.Actionable;
import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.bootstrap.web.component.Container;
import net.microfalx.bootstrap.web.util.ExtendedUserDetails;
import net.microfalx.bootstrap.web.util.Gravatar;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.thymeleaf.context.IContext;
import org.thymeleaf.extras.springsecurity6.util.SpringSecurityContextUtils;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.microfalx.lang.StringUtils.isEmpty;

/**
 * A tools which gives access to security context.
 */
public class SecurityTool extends AbstractTool implements Authentication {

    public static final String ANONYMOUS_USER = "anonymous";
    public static final String ANONYMOUS_NAME = "Guest";
    private Map<String, GrantedAuthority> authorities;

    public SecurityTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Authentication authentication = getDelegated();
        return authentication != null ? authentication.getAuthorities() : Collections.emptyList();
    }

    @Override
    public String getName() {
        Authentication authentication = getDelegated();
        if (authentication == null) return ANONYMOUS_NAME;
        Object rawPrincipal = authentication.getPrincipal();
        if (rawPrincipal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            return authenticatedPrincipal.getName();
        }
        if (rawPrincipal instanceof Principal principal) {
            return principal.getName();
        }
        return authentication.getName();
    }

    public String getEmail() {
        UserDetails userDetails = getUserDetails();
        if (userDetails instanceof ExtendedUserDetails extendedUserDetails) {
            return extendedUserDetails.getEmail();
        } else {
            return null;
        }
    }

    public String getImageUrl() {
        String url = null;
        UserDetails userDetails = getUserDetails();
        if (userDetails instanceof ExtendedUserDetails extendedUserDetails) {
            url = extendedUserDetails.getImageUrl();
        }
        if (StringUtils.isNotEmpty(url)) {
            return url;
        } else {
            return new Gravatar(getEmail()).getUrl();
        }
    }

    public String getUserName() {
        UserDetails userDetails = getUserDetails();
        if (userDetails != null) {
            return userDetails.getUsername();
        } else {
            return ANONYMOUS_USER;
        }
    }

    @Override
    public Object getCredentials() {
        Authentication authentication = getDelegated();
        return authentication != null ? authentication.getCredentials() : null;
    }

    @Override
    public Object getDetails() {
        Authentication authentication = getDelegated();
        return authentication != null ? authentication.getDetails() : null;
    }

    public boolean hasRole(String role) {
        if (isEmpty(role)) return true;
        Map<String, GrantedAuthority> cachedAuthorities = getCachedAuthorities();
        return cachedAuthorities.containsKey(role.toLowerCase());
    }

    public boolean hasRole(Set<String> roles) {
        if (ObjectUtils.isEmpty(roles)) return true;
        for (String role : roles) {
            if (hasRole(role)) return true;
        }
        return false;
    }

    public boolean hasRole(Actionable<?> actionable) {
        if (!hasRole(actionable.getRoles())) return false;
        if (actionable instanceof Container<?> container) {
            boolean hasAtLeastOne = false;
            for (Component<? extends Component<?>> child : container.getChildren()) {
                if (child instanceof Actionable<?> childActionable && hasRole(childActionable)) {
                    hasAtLeastOne = true;
                    break;
                }
            }
            return hasAtLeastOne;
        } else {
            return true;
        }
    }

    @Override
    public Object getPrincipal() {
        return getUserDetails();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = getDelegated();
        return authentication != null ? authentication.isAuthenticated() : false;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Authentication authentication = getDelegated();
        authentication.setAuthenticated(isAuthenticated);
    }

    private UserDetails getUserDetails() {
        Authentication authentication = getDelegated();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) return (UserDetails) principal;
        }
        org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(ANONYMOUS_USER, null, Collections.emptyList());
        return user;
    }

    private Map<String, GrantedAuthority> getCachedAuthorities() {
        if (authorities == null) {
            Authentication authentication = getDelegated();
            Collection<? extends GrantedAuthority> authorityCollection = authentication != null ? authentication.getAuthorities() : Collections.emptyList();
            authorities = authorityCollection.stream().collect(Collectors.toMap(grantedAuthority -> grantedAuthority.getAuthority().toLowerCase(), grantedAuthority -> grantedAuthority));
        }
        return authorities;
    }

    private Authentication getDelegated() {
        try {
            return SpringSecurityContextUtils.getAuthenticationObject(getWebContext());
        } catch (IllegalStateException e) {
            return SecurityContextHolder.getContext().getAuthentication();
        }
    }
}
