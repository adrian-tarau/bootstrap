package net.microfalx.bootstrap.web.application;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.web.application.annotation.SystemTheme;
import net.microfalx.bootstrap.web.application.annotation.Theme;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

@Configuration
@Slf4j
public class ApplicationMvcConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMvcConfig.class);

    private static final String THEME_QUERY_PARAMETER = "_theme";
    private static final String THEME_SESSION_ATTR = "$APPLICATION_THEME$";
    private static final String THEME_SESSION_DOMAIN_APPLIED_ATTR = "$APPLICATION_THEME_DOMAIN_APPLIED$";

    @Autowired
    private Application application;

    @Autowired
    private ApplicationService applicationService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestInterceptor());
    }

    @Order
    private class RequestInterceptor implements HandlerInterceptor {

        private String handleThemeFromRequest(HttpServletRequest request) {
            HttpSession session = request.getSession(false);
            String themeId = HttpMethod.valueOf(request.getMethod()) == HttpMethod.GET ? request.getParameter(THEME_QUERY_PARAMETER) : null;
            if (session != null) {
                if (isNotEmpty(themeId)) {
                    session.setAttribute(THEME_SESSION_ATTR, themeId);
                } else {
                    themeId = (String) session.getAttribute(THEME_SESSION_ATTR);
                    Boolean perDomainThemeApplied = (Boolean) session.getAttribute(THEME_SESSION_DOMAIN_APPLIED_ATTR);
                    String host = request.getServerName();
                    if (isEmpty(themeId) && isNotEmpty(host) && perDomainThemeApplied == null) {
                        Optional<net.microfalx.bootstrap.web.application.Theme> themeForDomain = applicationService.getThemeForDomain(host);
                        if (themeForDomain.isPresent()) {
                            themeId = themeForDomain.get().getId();
                            LOGGER.info("Applying theme '{}' for domain '{}'", themeForDomain.get().getId(), host);
                            session.setAttribute(THEME_SESSION_ATTR, themeId);
                            session.setAttribute(THEME_SESSION_DOMAIN_APPLIED_ATTR, Boolean.TRUE);
                        }
                    }
                }
            }
            return themeId;
        }

        private String handleThemeFromHandler(Object handler) {
            Class<?> handlerClass = null;
            if (handler instanceof HandlerMethod) handlerClass = ((HandlerMethod) handler).getBeanType();
            if (handlerClass == null) return null;
            String themeId = null;
            Theme themeAnnot = AnnotationUtils.getAnnotation(handlerClass, Theme.class);
            if (themeAnnot == null) {
                SystemTheme systemThemeAnnot = AnnotationUtils.getAnnotation(handlerClass, SystemTheme.class);
                if (systemThemeAnnot != null) themeId = application.getSystemTheme().getId();
            } else {
                themeId = themeAnnot.value();
            }
            return themeId;
        }

        private void handleTheme(HttpServletRequest request, Object handler) {
            String themeId = handleThemeFromRequest(request);
            if (isEmpty(themeId)) themeId = handleThemeFromHandler(handler);
            if (isNotEmpty(themeId)) {
                try {
                    net.microfalx.bootstrap.web.application.Theme theme = applicationService.getTheme(themeId);
                    ApplicationService.THEME.set(theme);
                } catch (ApplicationException e) {
                    LOGGER.warn("A theme with identifier '{}' is not registered", themeId);
                } catch (Exception e) {
                    LOGGER.atError().setCause(e).log("Failed to extract theme with identifier '{}' from controller '{}'",
                            themeId, ClassUtils.getName(handler));
                }
            }
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            handleTheme(request, handler);
            ApplicationService.APPLICATION.set(StringUtils.defaultIfEmpty(request.getHeader("X-Application-Id"), "na"));
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            ApplicationService.THEME.remove();
        }
    }
}
