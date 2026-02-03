package net.microfalx.bootstrap.feature;

import net.microfalx.lang.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

class FeatureHandlerInterceptor implements HandlerInterceptor {

    private final FeatureService featureService;

    FeatureHandlerInterceptor(FeatureService featureService) {
        this.featureService = featureService;
    }

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) throws Exception {
        FeatureContext featureContext = FeatureContext.get();
        activateFeatures(featureContext);
        Class<?> handlerClass = null;
        if (handler instanceof HandlerMethod) handlerClass = ((HandlerMethod) handler).getBeanType();
        if (handlerClass != null) handleFeaturesFromHandler(handlerClass, featureContext);
        return true;
    }

    @Override
    public void afterCompletion(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler, Exception ex) throws Exception {
        FeatureContext.reset();
    }

    private void activateFeatures(FeatureContext featureContext) {
        featureService.getEnabledFeatures().forEach(featureContext::enable);
    }

    private void handleFeaturesFromHandler(Class<?> handlerClass, FeatureContext featureContext) {
        net.microfalx.bootstrap.feature.annotation.Feature featureAnnot = AnnotationUtils.getAnnotation(handlerClass, net.microfalx.bootstrap.feature.annotation.Feature.class);
        if (featureAnnot != null) enableFeature(featureAnnot.value(), featureContext);
        net.microfalx.bootstrap.feature.annotation.Features featuresAnnot = AnnotationUtils.getAnnotation(handlerClass, net.microfalx.bootstrap.feature.annotation.Features.class);
        if (featuresAnnot != null) {
            for (net.microfalx.bootstrap.feature.annotation.Feature feature : featuresAnnot.value()) {
                enableFeature(feature.value(), featureContext);
            }
        }
    }

    private void enableFeature(String id, FeatureContext featureContext) {
        Optional<net.microfalx.bootstrap.feature.Feature> feature = featureService.findFeature(id);
        feature.ifPresent(featureContext::enable);
    }
}
