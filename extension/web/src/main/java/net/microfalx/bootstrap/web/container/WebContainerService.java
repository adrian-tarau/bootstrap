package net.microfalx.bootstrap.web.container;

import jakarta.servlet.ServletContext;
import net.microfalx.bootstrap.web.util.HttpServletUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.threadpool.ThreadPool;
import net.microfalx.threadpool.Trigger;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A service which gives access to some of the web container features.
 */
@Service
public final class WebContainerService implements InitializingBean {

    @Autowired private ServletContext servletContext;
    @Autowired ThreadPool threadPool;

    /**
     * Returns a relative path to access a web resource.
     *
     * @param path the relative path to the base URL
     * @return the absolute path
     */
    public String getPath(String path) {
        return getPath(path, Collections.emptyMap());
    }

    /**
     * Returns a relative path to access a web resource.
     *
     * @param path   the relative path to the container base path
     * @param params the HTTP parameters to be passed with the URL
     * @return the absolute path
     */
    public String getPath(String path, Map<String, Object> params) {
        String fullPath = StringUtils.addStartSlash(StringUtils.addStartSlash(servletContext.getContextPath()));
        if (StringUtils.isNotEmpty(path)) fullPath += StringUtils.removeStartSlash(path);
        if (!path.isEmpty() && !params.isEmpty()) {
            List<BasicNameValuePair> pairs = params.entrySet().stream().map(e -> new BasicNameValuePair(e.getKey(), ObjectUtils.toString(e.getValue()))).toList();
            fullPath += "?" + URLEncodedUtils.format(pairs, StandardCharsets.UTF_8);
        }
        return fullPath;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initTasks();
    }

    private void initTasks() {
        threadPool.schedule(new MaintenanceTask(), Trigger.fixedDelay(Duration.ofSeconds(60)));
    }

    private void clearRequestFailures() {
        HttpServletUtils.cleanupFailures();
    }

    static {
        HttpServletUtils.init();
    }

    class MaintenanceTask implements Runnable {

        @Override
        public void run() {
            clearRequestFailures();
        }
    }
}
