package net.microfalx.bootstrap.restapi.client;

import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.threadpool.ThreadPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A service which tracks REST client interactions.
 */
@Service
public class RestClientService implements InitializingBean {

    @Autowired(required = false) private RestClientProperties properties = new RestClientProperties();

    private ThreadPool threadPool;

    private OkHttpClient httpClient;
    private final Map<URI, RestClient> clients = new ConcurrentHashMap<>();
    private final Map<String, RestApiAudit> auditsPending = new ConcurrentHashMap<>();
    private final Queue<RestApiAudit> audits = new ArrayBlockingQueue<>(100);
    private final Queue<RestApiAudit> auditsForPersistence = new ConcurrentLinkedQueue<>();

    @Autowired private RestApiAuditPersister persister;

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
        RestClient restClient = new RestClient(this, httpClient).setUri(uri);
        restClient.setApiKey(apiKey);
        clients.put(uri, restClient);
        return restClient;
    }

    /**
     * Returns the pending audit entries.
     *
     * @return a non-null instance
     */
    public Collection<RestApiAudit> getPending() {
        return unmodifiableCollection(auditsPending.values());
    }

    /**
     * Returns the last N audit entries.
     *
     * @return a non-null instance
     */
    public Collection<RestApiAudit> getCompleted() {
        return unmodifiableCollection(audits);
    }

    /**
     * Logs an audit entry.
     *
     * @param audit the entry
     */
    void auditStart(RestApiAudit audit) {
        requireNonNull(audit);
        auditsPending.put(audit.getRequestId(), audit);
    }

    /**
     * Logs an audit entry.
     *
     * @param audit the entry
     */
    void auditEnd(RestApiAudit audit) {
        requireNonNull(audit);
        auditsPending.remove(audit.getRequestId());
        // Remove the oldest entry until there is space
        while (!audits.offer(audit)) audits.poll();
        RestClient client = RestClient.CLIENT.get();
        if (client != null) client.auditEnd(audit);
        auditsForPersistence.offer(audit);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initPersister();
        initThreadPool();
        initHttpClient();
    }

    private void initPersister() {
        persister.init();
    }

    private void initThreadPool() {
        if (threadPool == null) {
            threadPool = ThreadPoolFactory.create("RestClient").setRatio(2).create();
        }
        threadPool.scheduleAtFixedRate(new PersistAuditsTask(), Duration.ofSeconds(5));
    }

    private void initHttpClient() {
        RestClientLogger logger = new RestClientLogger(properties.getLoggingLevel());
        RestClientRetryInterceptor retryInterceptor = new RestClientRetryInterceptor(properties.getMaximumRetries());
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout()).readTimeout(properties.getReadTimeout())
                .writeTimeout(properties.getWriteTimeout()).callTimeout(properties.getCallTimeout())
                .retryOnConnectionFailure(true);

        // first interceptor is the audit one
        builder.addInterceptor(new RestApiAuditInterceptor(this));
        // then the rest
        builder.addInterceptor(logger.create())
                .addInterceptor(retryInterceptor)
                .addInterceptor(new RestClientApiKeyInterceptor())
                .build();
        builder.dispatcher(new Dispatcher(threadPool));
        httpClient = builder.build();
    }

    private class PersistAuditsTask implements Runnable {

        @Override
        public void run() {
            while (!auditsForPersistence.isEmpty()) {
                RestApiAudit audit = auditsForPersistence.poll();
                if (audit != null) persister.persistAudit(audit);
            }
        }
    }
}
