package net.microfalx.bootstrap.web.template;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.support.RequestDataValueProcessor;

import java.util.Collections;
import java.util.Map;

public class TemplateDataValueProcessor implements RequestDataValueProcessor {

    @Override
    public String processAction(HttpServletRequest request, String action, String httpMethod) {
        return action;
    }

    @Override
    public String processFormFieldValue(HttpServletRequest request, String name, String value, String type) {
        return value;
    }

    @Override
    public Map<String, String> getExtraHiddenFields(HttpServletRequest request) {
        return Collections.emptyMap();
    }

    @Override
    public String processUrl(HttpServletRequest request, String url) {
        return url;
    }
}
