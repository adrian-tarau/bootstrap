package net.microfalx.bootstrap.web.application;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.bootstrap.web.application.annotation.SystemTheme;
import net.microfalx.bootstrap.web.application.annotation.Theme;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static net.microfalx.lang.StringUtils.isNotEmpty;

@Configuration
public class ApplicationMvcConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMvcConfig.class);

    @Autowired
    private Application application;

    @Autowired
    private ApplicationService applicationService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestInterceptor());
    }

    @Order()
    private class RequestInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            Class<?> handlerClass = null;
            if (handler instanceof HandlerMethod) handlerClass = ((HandlerMethod) handler).getBeanType();
            String themeId = null;
            boolean system = false;
            Theme themeAnnot = AnnotationUtils.getAnnotation(handlerClass, Theme.class);
            if (themeAnnot == null) {
                SystemTheme systemThemeAnnot = AnnotationUtils.getAnnotation(handlerClass, SystemTheme.class);
                if (systemThemeAnnot != null) system = true;
            } else {
                themeId = themeAnnot.value();
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
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            ApplicationService.THEME.remove();
        }
    }
}
