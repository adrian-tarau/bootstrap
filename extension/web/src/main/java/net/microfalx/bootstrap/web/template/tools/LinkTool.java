package net.microfalx.bootstrap.web.template.tools;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Tools around links.
 */
@SuppressWarnings("unused")
public class LinkTool extends AbstractTool {

    public LinkTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
    }

    /**
     * Returns the path which identifies the current request.
     *
     * @return a non-null instance
     */
    public String getSelf() {
        return getWebContext().getExchange().getRequest().getRequestPath();
    }

    /**
     * Returns the full URL which identifies the request.
     *
     * @return a non-null instance
     */
    public String getUrl() {
        return getWebContext().getExchange().getRequest().getRequestURL();
    }

    /**
     * Returns the query parameters.
     *
     * @return a non-null instance
     */
    public Map<String, Object> getQuery() {
        Map<String, Object> params = new HashMap<>();
        Map<String, String[]> originalParams = getWebContext().getExchange().getRequest().getParameterMap();
        originalParams.forEach((k, v) -> {
            if (v != null && v.length == 1) {
                params.put(k, v[0]);
            } else {
                params.put(k, v);
            }
        });
        return params;
    }
}
