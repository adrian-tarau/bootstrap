package net.microfalx.bootstrap.restapi;

import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.restapi.client.RestClient;
import net.microfalx.bootstrap.restapi.client.RestClientService;
import net.microfalx.bootstrap.restapi.client.exception.ApiException;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static net.microfalx.bootstrap.restapi.RestApiUtils.VALIDATION_METRICS;
import static net.microfalx.bootstrap.restapi.RestApiUtils.VALIDATION_STATUS_METRICS;
import static net.microfalx.lang.StringUtils.*;

@Service
@Slf4j
public class RestApiService implements ApplicationListener<WebServerInitializedEvent> {

    @Autowired private ThreadPool threadPool;
    @Autowired private RestClientService restClientService;
    @Autowired private ServletContext servletContext;
    @Autowired(required = false) private RestApiProperties properties = new RestApiProperties();

    private RestClient restClient;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        URI uri;
        String basePath = "api/v1/";
        if (isNotEmpty(properties.getPublicUrl())) {
            uri = URI.create(addEndSlash(properties.getPublicUrl()) + basePath);
        } else {
            String path = addEndSlash(addStartSlash(servletContext.getContextPath())) + basePath;
            uri = URI.create("http://localhost:" + event.getWebServer().getPort() + path);
        }
        try {
            restClient = restClientService.register(uri, null).setName("Local");
            threadPool.scheduleAtFixedRate(new ValidateRestApiEndPointsTask(), properties.getValidationInterval());
            threadPool.schedule(new ValidateRestApiEndPointsTask(), 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("Failed to register local REST client for URI {}", uri, e);
        }
    }

    private class ValidateRestApiEndPointsTask implements Runnable {

        @Override
        public void run() {
            RestApiStatusApi statusApi = restClient.create(RestApiStatusApi.class);
            VALIDATION_METRICS.time("Status", (t) -> {
                int status = 200;
                try {
                    if (!"OK".equalsIgnoreCase(statusApi.status())) {
                        status = 502;
                    }
                } catch (ApiException e) {
                    status = e.getStatus();
                } catch (Exception e) {
                    status = 503;
                }
                VALIDATION_STATUS_METRICS.count(Integer.toString(status));
            });
        }
    }

}
