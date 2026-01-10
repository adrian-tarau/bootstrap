package net.microfalx.bootstrap.web.application;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

import static net.microfalx.lang.StringUtils.isNotEmpty;

@Configuration
public class ApplicationMvcConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMvcConfig.class);

    private static final String THEME_QUERY_PARAMETER = "_theme";
    private static final String THEME_SESSION_ATTR = "_theme";

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

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            Class<?> handlerClass = null;
            HttpSession session = request.getSession(false);
            if (handler instanceof HandlerMethod) handlerClass = ((HandlerMethod) handler).getBeanType();
            String themeId = HttpMethod.valueOf(request.getMethod()) == HttpMethod.GET ? request.getParameter(THEME_QUERY_PARAMETER) : null;
            if (StringUtils.isNotEmpty(themeId)) {
                if (session != null) session.setAttribute(THEME_SESSION_ATTR, themeId);
            }
            if (session != null) themeId = (String) session.getAttribute(THEME_SESSION_ATTR);
            boolean system = false;
            if (StringUtils.isEmpty(themeId)) {
                Theme themeAnnot = handlerClass != null ? AnnotationUtils.getAnnotation(handlerClass, Theme.class) : null;
                if (themeAnnot == null) {
                    SystemTheme systemThemeAnnot = handlerClass != null ? AnnotationUtils.getAnnotation(handlerClass, SystemTheme.class) : null;
                    if (systemThemeAnnot != null) system = true;
                } else {
                    themeId = themeAnnot.value();
                }
            }
            if (isNotEmpty(themeId) || system) {
                try {
                    net.microfalx.bootstrap.web.application.Theme theme = system ? application.getSystemTheme() : applicationService.getTheme(themeId);
                    ApplicationService.THEME.set(theme);
                } catch (ApplicationException e) {
                    LOGGER.error("A theme with identifier '" + themeId + "' is not registered");
                } catch (Exception e) {
                    LOGGER.error("Failed to extract theme with identifier '" + themeId + "' from controller '" + ClassUtils.getName(handler), e);
                }
            }
            ApplicationService.APPLICATION.set(StringUtils.defaultIfEmpty(request.getHeader("X-Application-Id"), "na"));
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            ApplicationService.THEME.remove();
        }
    }
}
