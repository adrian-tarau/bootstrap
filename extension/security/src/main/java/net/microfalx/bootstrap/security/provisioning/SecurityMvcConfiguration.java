package net.microfalx.bootstrap.security.provisioning;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Configuration
@Slf4j
public class SecurityMvcConfiguration implements WebMvcConfigurer {

    private final Collection<RequestMatcher> anonymous = new CopyOnWriteArrayList<>();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        initMatchers();
        registry.addInterceptor(new Interceptor());
    }

    public void registerAnonymous(RequestMatcher matcher) {
        requireNonNull(matcher);
        anonymous.add(matcher);
    }

    private boolean applySecurity(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        Rule rule = findRule(request, handlerMethod);
        LOGGER.debug("Request '{}' mapped to method '{}' with security rule: {}", request.getRequestURI(), describe(handlerMethod), rule);
        if (!rule.authenticated) return true;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        requireAuthenticated(authentication, handlerMethod);
        if (rule.roles.length > 0) {
            requireRoles(authentication, rule.roles, handlerMethod);
        }
        return true;
    }

    private Rule findRule(HttpServletRequest request, HandlerMethod hm) {
        if (isAnonymous(request)) return NO_SECURITY;
        PermitAll permitAllAnnot = hm.getMethodAnnotation(PermitAll.class);
        if (permitAllAnnot != null) return NO_SECURITY;
        Class<?> controllerType = hm.getBeanType();
        while (controllerType != null) {
            permitAllAnnot = AnnotationUtils.getAnnotation(controllerType, PermitAll.class, true);
            if (permitAllAnnot != null) return NO_SECURITY;
            RolesAllowed rolesAllowedAnnot = AnnotationUtils.getAnnotation(controllerType, RolesAllowed.class, true);
            if (rolesAllowedAnnot != null) return new Rule(rolesAllowedAnnot.value());
            controllerType = controllerType.getSuperclass();
        }
        return SECURITY_ANY_ROLE;
    }

    private void requireAuthenticated(Authentication auth, HandlerMethod handlerMethod) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Method '" + describe(handlerMethod) + "' requires authentication");
        }
    }

    private void requireRoles(Authentication auth, String[] roles, HandlerMethod handlerMethod) {
        Set<String> granted = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        for (String role : roles) {
            if (!granted.contains(role)) {
                throw new AccessDeniedException("Method '" + describe(handlerMethod) + "' requires role '" + role + "'");
            }
        }
    }

    private String describe(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    private void initMatchers() {
        registerAnonymous(PathPatternRequestMatcher.withDefaults().matcher("/login/**"));
        registerAnonymous(PathPatternRequestMatcher.withDefaults().matcher("/logout/**"));
    }

    private boolean isAnonymous(HttpServletRequest request) {
        for (RequestMatcher matcher : anonymous) {
            if (matcher.matches(request)) return true;
        }
        return false;
    }

    private static final Rule NO_SECURITY = new Rule(false);
    private static final Rule SECURITY_ANY_ROLE = new Rule(true);

    @ToString
    private static class Rule {

        private final boolean authenticated;
        private String[] roles = StringUtils.EMPTY_STRING_ARRAY;

        private Rule(boolean authenticated) {
            this.authenticated = authenticated;
        }

        private Rule(String[] roles) {
            this.authenticated = true;
            this.roles = roles;
        }
    }

    @Order
    private class Interceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            boolean proceed = true;
            if (handler instanceof HandlerMethod) {
                proceed = applySecurity(request, response, ((HandlerMethod) handler));
            }
            return proceed;
        }

    }
}
