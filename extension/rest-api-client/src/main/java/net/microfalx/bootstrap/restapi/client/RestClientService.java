package net.microfalx.bootstrap.restapi.client;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A service which tracks REST client interactions.
 */
public class RestClientService implements InitializingBean {

    @Autowired(required = false)
    private RestClientProperties properties = new RestClientProperties();

    private OkHttpClient httpClient;
    private final Map<URI, RestClient> clients = new ConcurrentHashMap<>();

    /**
     * Returns the registered clients.
     *
     * @return a non-null instance
     */
    public Collection<RestClient> getClients() {
        return unmodifiableCollection(clients.values());
    }

    /**
     * Registers a client for the given URI and API key.
     *
     * @param uri    the base URI
     * @param apiKey the API key
     * @return the registered client
     */
    public RestClient register(URI uri, String apiKey) {
        requireNonNull(uri);
        requireNonNull(apiKey);
        RestClient restClient = new RestClient(httpClient).setUri(uri);
        restClient.setApiKey(apiKey);
        clients.put(uri, restClient);
        return restClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initHttpClient();
    }

    private void initHttpClient() {
        RestClientLogger logger = new RestClientLogger(properties.getLoggingLevel());
        RestClientRetryInterceptor retryInterceptor = new RestClientRetryInterceptor(properties.getMaximumRetries());
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout())
                .writeTimeout(properties.getWriteTimeout())
                .callTimeout(properties.getCallTimeout())
                .retryOnConnectionFailure(true)
                .addInterceptor(logger.create())
                .addInterceptor(retryInterceptor)
                .addInterceptor(new RestClientApiKeyInterceptor())
                .build();
    }

}
