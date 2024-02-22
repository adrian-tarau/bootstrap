package net.microfalx.bootstrap.web.container;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.bootstrap.dataset.formatter.FormatterUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebContainerMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestInterceptor());
    }

    @Order()
    private static class RequestInterceptor implements HandlerInterceptor {

        @SuppressWarnings("ConstantValue")
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            WebContainerRequest.REQUEST.set(request);
            WebContainerRequest containerRequest = WebContainerRequest.get();
            FormatterUtils.setTimeZone(containerRequest.getTimeZone());
            FormatterUtils.setLocale(containerRequest.getLocale());
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            FormatterUtils.setTimeZone(null);
            FormatterUtils.setLocale(null);
            WebContainerRequest.REQUEST.remove();
        }


    }
}
