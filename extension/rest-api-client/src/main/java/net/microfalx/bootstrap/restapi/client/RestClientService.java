package net.microfalx.bootstrap.restapi.client;

import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.metrics.Metrics;
import net.microfalx.metrics.Timer;
import net.microfalx.threadpool.ThreadPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.RsaAlgorithm;
import org.springframework.security.crypto.encrypt.RsaSecretEncryptor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * A service which tracks REST client interactions.
 */
@Service
public class RestClientService implements InitializingBean, TextEncryptor {

    @Autowired(required = false) private RestClientProperties properties = new RestClientProperties();

    private ThreadPool threadPool;

    private OkHttpClient httpClient;
    private final Map<URI, RestClient> clients = new ConcurrentHashMap<>();
    private final Map<String, Long> clientLastReload = new ConcurrentHashMap<>();
    private final Map<String, RestApiAudit> auditsPending = new ConcurrentHashMap<>();
    private final Queue<RestApiAudit> audits = new ArrayBlockingQueue<>(100);
    private final Queue<RestApiAudit> auditsForPersistence = new ConcurrentLinkedQueue<>();

    private TextEncryptor textEncryptor;

    private static final String ENCRYPTION_PREFIX = "{rsa}";
    private static final String ENCRYPTION_SALT = "jc1Ynhul3q5kyMU8j7cY9RRrD43CI3cg";
    private static final Metrics METRICS = Metrics.of("REST Client");

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
     * @param apiKey the API key, can be null
     * @return the registered client
     */
    public RestClient register(URI uri, String apiKey) {
        requireNonNull(uri);
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

    @Override
    public String encrypt(String text) {
        if (isEmpty(text)) return null;
        return ENCRYPTION_PREFIX + textEncryptor.encrypt(text);
    }

    @Override
    public String decrypt(String encryptedText) {
        if (isEmpty(encryptedText)) return null;
        if (encryptedText.startsWith(ENCRYPTION_PREFIX)) {
            return textEncryptor.decrypt(encryptedText.substring(ENCRYPTION_PREFIX.length()));
        } else {
            throw new IllegalArgumentException("Invalid encrypted text: " + encryptedText);
        }
    }

    /**
     * Registers an audit entry in progress.
     *
     * @param audit the entry
     */
    void auditStart(RestApiAudit audit) {
        requireNonNull(audit);
        auditsPending.put(audit.getRequestId(), audit);
    }

    /**
     * Tracks a completed audit entry.
     *
     * @param audit the entry
     */
    void auditEnd(RestApiAudit audit) {
        requireNonNull(audit);
        auditsPending.remove(audit.getRequestId());
        // Remove the oldest entry until there is space
        while (!audits.offer(audit)) audits.poll();
        RestClient client = audit.getClient();
        if (client != null) {
            client.auditEnd(audit);
            auditsForPersistence.offer(audit);
            Timer timer = METRICS.withGroup(client.getName()).getTimer(audit.getRequestPattern(), Timer.Type.SHORT_PERCENTILE);
            timer.record(audit.getDuration());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initEncryptor();
        initPersister();
        initThreadPool();
        initHttpClient();
    }

    private void initPersister() {
        persister.init(this);
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
        builder.addInterceptor(new RestApiHeadersInterceptor())
                .addInterceptor(logger.create())
                .addInterceptor(retryInterceptor)
                .build();
        builder.dispatcher(new Dispatcher(threadPool));
        httpClient = builder.build();
    }

    void reloadIfRequired(RestClient restClient) {
        String clientId = restClient.getId();
        long lastReload = clientLastReload.getOrDefault(clientId, currentTimeMillis());
        if (millisSince(lastReload) > properties.getReloadInterval().toMillis()) {
            synchronized (clientLastReload) {
                if (millisSince(lastReload) > properties.getReloadInterval().toMillis()) {
                    clientLastReload.put(clientId, currentTimeMillis());
                    threadPool.execute(new ReloadApiKeyTask(restClient));
                }
            }
        }
    }

    private void initEncryptor() {
        textEncryptor = new RsaSecretEncryptor(RsaAlgorithm.DEFAULT, ENCRYPTION_SALT);
    }

    private class ReloadApiKeyTask implements Runnable {

        private final RestClient restClient;

        public ReloadApiKeyTask(RestClient restClient) {
            this.restClient = restClient;
        }

        @Override
        public void run() {
            String apiKey = persister.getApiKey(restClient);
            if (apiKey != null) {
                restClient.setApiKey(textEncryptor.decrypt(apiKey));
            }
        }
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
