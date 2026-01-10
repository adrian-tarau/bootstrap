package net.microfalx.bootstrap.security.user;

import lombok.ToString;
import net.microfalx.bootstrap.security.SecurityConstants;
import net.microfalx.bootstrap.security.SecurityContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.bootstrap.security.SecurityUtils.getCurrentPrincipal;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@ToString
public class SecurityContextImpl implements SecurityContext {

    private static final User ANONYMOUS = UserImpl.builder().userName("anonymous").name("Anonymous").build();

    private static final String id = UUID.randomUUID().toString();
    private User user = ANONYMOUS;
    private Principal principal;
    private UserDetails userDetails;
    private Authentication authentication;
    private org.springframework.security.core.context.SecurityContext securityContext;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Set<String> roles = ConcurrentHashMap.newKeySet();

    public static ThreadLocal<SecurityContext> CONTEXT = ThreadLocal.withInitial(SecurityContextImpl::new);

    SecurityContextImpl() {
    }

    SecurityContextImpl(User user, org.springframework.security.core.context.SecurityContext securityContext) {
        requireNonNull(user);
        this.user = user;
        this.securityContext = securityContext;
        initialize();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return getUser().getName();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public boolean hasRole(String role) {
        requireNonNull(role);
        return roles.contains(role.toLowerCase());
    }

    @Override
    public Principal getPrincipal() {
        return user;
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        throw new IllegalStateException("Should not be called directly");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(String name) {
        requireNonNull(name);
        return (T) attributes.get(name);
    }

    @Override
    public <T> void setAttribute(String name, T value) {
        requireNonNull(name);
        attributes.put(name, value);
    }

    private void initialize() {
        initializeSpringSecurity();
        initializeUser();
        initializeRoles();
    }

    private void initializeSpringSecurity() {
        if (securityContext != null) {
            this.authentication = securityContext.getAuthentication();
        }
        if (this.authentication == null) {
            this.authentication = new AnonymousAuthenticationToken(SecurityConstants.ANONYMOUS_USER, getCurrentPrincipal(), Collections.emptyList());
        }
        if (this.authentication.getPrincipal() instanceof UserDetails userDetailsFromPrincipal) {
            this.userDetails = userDetailsFromPrincipal;
        }
    }

    private void initializeRoles() {
        this.roles.addAll(this.authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull).map(String::toLowerCase)
                .toList());
    }

    private void initializeUser() {
        this.user = UserImpl.builder().name(this.user.getUsername())
                .displayName(this.user.getName())
                .userName(this.user.getUsername())
                .email(this.user.getEmail())
                .enabled(this.user.isEnabled())
                .description(this.user.getDescription()).build();
    }
}
