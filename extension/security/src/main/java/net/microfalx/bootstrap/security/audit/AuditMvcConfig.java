package net.microfalx.bootstrap.security.audit;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.microfalx.bootstrap.security.user.UserService;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Action;
import net.microfalx.lang.annotation.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static net.microfalx.lang.StringUtils.*;

@Configuration
public class AuditMvcConfig implements WebMvcConfigurer {

    private final Set<String> excludedPaths = new CopyOnWriteArraySet<>();

    @Autowired
    private UserService userService;

    public AuditMvcConfig() {
        registerDefaultPaths();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditInterceptor());
    }

    private void audit(HttpServletRequest request, HttpServletResponse response, Method method) {
        String path = request.getRequestURI();
        if (shouldExclude(path)) return;
        AuditContext context = AuditContext.get();
        context.setReference(path);
        context.setErrorCode(Integer.toString(response.getStatus()));
        String clientInfo = request.getRemoteHost();
        if (isNotEmpty(request.getRemoteUser())) clientInfo = request.getRemoteUser() + "@" + clientInfo;
        context.setClientInfo(clientInfo);
        if (isEmpty(context.getAction())) context.setAction(getAction(method));
        if (isEmpty(context.getModule())) context.setModule(getModule(method));
        userService.audit(context);
    }

    private String getAction(Method method) {
        String action = StringUtils.capitalize(method.getName());
        Action nameAnnot = method.getAnnotation(Action.class);
        if (nameAnnot != null) action = nameAnnot.value();
        Operation operationAnnot = method.getAnnotation(Operation.class);
        if (operationAnnot != null) action = operationAnnot.summary();
        return action;
    }

    private String getModule(Method method) {
        String module = method.getDeclaringClass().getSimpleName();
        module = replaceFirst(module, "Controller", StringUtils.EMPTY_STRING);
        Module nameAnnot = method.getAnnotation(Module.class);
        if (nameAnnot != null) module = nameAnnot.value();
        return module;
    }

    private void registerExclusion(String path) {
        excludedPaths.add(removeStartSlash(path).toLowerCase());
    }

    private void registerDefaultPaths() {
        registerExclusion("asset");
        registerExclusion("error");
        registerExclusion("favicon.ico");
    }

    private boolean shouldExclude(String path) {
        path = removeStartSlash(path).toLowerCase();
        for (String excludedPath : excludedPaths) {
            if (path.startsWith(excludedPath)) return true;
        }
        return false;
    }

    private class AuditInterceptor implements HandlerInterceptor {

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            if (handler instanceof HandlerMethod) {
                Method method = ((HandlerMethod) handler).getMethod();
                audit(request, response, method);
            }
        }


    }
}
