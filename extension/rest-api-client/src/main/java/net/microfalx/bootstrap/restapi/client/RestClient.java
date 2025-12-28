package net.microfalx.bootstrap.restapi.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.microfalx.bootstrap.restapi.client.exception.*;
import net.microfalx.lang.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Collection;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A REST client which can be used to create API wrappers.
 * <p>
 * Behind the scenes, it uses <a href="https://square.github.io/retrofit/">Retrofit</a> and
 * <a href="https://square.github.io/okhttp/">OkHttp</a>.
 */
public class RestClient implements Identifiable<String>, Nameable, Descriptable {

    private final RestClientService restClientService;
    private final OkHttpClient client;

    private ObjectMapper objectMapper;
    private static final String DEFAULT_NAME = "RestClient";

    private String id = UUID.randomUUID().toString();
    private String name = DEFAULT_NAME;
    private String description;
    private URI uri;
    private Retrofit retrofit;

    private String apiKeyHeaderName = "X-API-Key";
    private String apiKey;
    private boolean useHeader = true;
    private final Queue<RestApiAudit> audits = new ArrayBlockingQueue<>(100);

    static ThreadLocal<RestClient> CLIENT = new ThreadLocal<>();

    RestClient(RestClientService restClientService, OkHttpClient client) {
        requireNonNull(restClientService);
        requireNonNull(client);
        this.restClientService = restClientService;
        this.client = client;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Changes the name of the client.
     * <p>
     * By default, the client name is the hostname of the base URI.
     *
     * @param name the new name
     * @return self
     */
    public RestClient setName(String name) {
        ArgumentUtils.requireNotEmpty(name);
        this.name = name;
        return this;
    }

    /**
     * Returns the description of the client.
     *
     * @return a non-null instance
     */
    public String getDescription() {
        return description;
    }

    /**
     * Changes the description of the client.
     *
     * @param description the new description
     */
    public RestClient setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Returns the underlying HTTP client.
     *
     * @return a non-null client
     */
    public OkHttpClient getClient() {
        return client;
    }

    /**
     * Returns the base URI.
     *
     * @return the non-null URI
     */
    public URI getUri() {
        if (uri == null) throw new RestClientException("The base URI has not been set");
        return uri;
    }

    /**
     * Changes the base URI.
     *
     * @param uri the new base URI
     * @return self
     */
    public RestClient setUri(URI uri) {
        requireNonNull(uri);
        this.uri = uri;
        this.name = StringUtils.defaultIfEmpty(uri.getHost(), DEFAULT_NAME);
        updateId();
        return this;
    }

    /**
     * Returns the API key to be used for authentication.
     *
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Changes the API key to be used for authentication.
     *
     * @param apiKey the new API key
     * @return self
     */
    public RestClient setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Returns the name of the header used to pass the API key
     *
     * @return a non-null instance
     */
    public String getApiKeyHeaderName() {
        return apiKeyHeaderName;
    }

    /**
     * Changes the name of the header used to pass the API key.
     * <p>
     * By default, the header name is "X-API-Key".
     *
     * @param apiKeyHeaderName the new header name
     * @return self
     */
    public RestClient setApiKeyHeaderName(String apiKeyHeaderName) {
        this.apiKeyHeaderName = apiKeyHeaderName;
        return this;
    }

    /**
     * Returns whether the API key should be passed as a header.
     *
     * @return {@code true} if the API key should be passed as a header, {@code false} if it should be passed as a Bearer token
     * @see #getApiKeyHeaderName()
     */
    public boolean isUseHeader() {
        return useHeader;
    }

    /**
     * Changes whether the API key should be passed as a header.
     *
     * @param useHeader {@code true} if the API key should be passed as a header, {@code false} if it should be passed as a Bearer token
     * @return self
     */
    public RestClient setUseHeader(boolean useHeader) {
        this.useHeader = useHeader;
        return this;
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
     * Return the Retrofit instance used to create API wrappers.
     *
     * @return a non-null instance
     */
    public Retrofit getWrapper() {
        if (retrofit == null) {
            try {
                Retrofit.Builder builder = new Retrofit.Builder().baseUrl(uri.toURL()).client(client);
                builder.addConverterFactory(JacksonConverterFactory.create(getObjectMapper()));
                builder.addCallAdapterFactory(new RestApiCallAdapter.Factory(this));
                retrofit = builder.build();
            } catch (Exception e) {
                throw new RestClientException("Failed to create Rest API wrapper", e);
            }
        }
        return retrofit;
    }

    /**
     * Executes the call and maps errors to exceptions.
     *
     * @param call the call to execute
     * @param <T>  the response type
     * @return the response body
     */
    <T> T execute(Call<T> call) {
        CLIENT.set(this);
        try {
            Response<T> response = call.execute();
            if (!response.isSuccessful()) throw map(response);
            return response.body();
        } catch (IOException e) {
            throw new ServerErrorException(500, new ApiError().setStatus(500).setMessage(e.getMessage()));
        } finally {
            CLIENT.remove();
        }
    }

    /**
     * Logs an audit entry.
     *
     * @param audit the entry
     */
    void auditEnd(RestApiAudit audit) {
        requireNonNull(audit);
        // Remove the oldest entry until there is space
        while (!audits.offer(audit)) audits.poll();
    }

    /**
     * Creates an API wrapper for the given API interface.
     *
     * @param apiClass the API interface class
     * @param <A>      the API type
     * @return the API wrapper
     * @see #execute(Call)
     */
    @SuppressWarnings("unchecked")
    public <A> A create(Class<A> apiClass) {
        A wrapper = getWrapper().create(apiClass);
        return (A) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{apiClass}, new RestApiWrapper<>(this, apiClass, wrapper));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RestClient.class.getSimpleName() + "[", "]")
                .add("uri=" + uri)
                .add("useHeader=" + useHeader)
                .add("apiKeyHeaderName='" + apiKeyHeaderName + "'")
                .add("apiKey='" + SecretUtils.maskSecret(apiKey) + "'")
                .toString();
    }

    /**
     * Maps a Retrofit response to an exception.
     *
     * @param response the response
     * @return the exception
     */
    ApiException map(retrofit2.Response<?> response) {
        ApiError apiError = null;
        try (ResponseBody body = response.errorBody()) {
            if (body != null & JSON_MEDIA_TYPE.equals(body.contentType())) {
                apiError = getObjectMapper().readValue(body.byteStream(), ApiError.class);
            }
        } catch (Exception ignored) {
            // Non-JSON error body
        }
        int status = response.code();
        if (apiError == null) {
            apiError = new ApiError().setMessage(response.message());
        }
        apiError.setStatus(status);
        return switch (status) {
            case 400, 405, 406, 422 -> new BadRequestException(status, apiError);
            case 409 -> new ConflictException(status, apiError);
            case 401, 403 -> new UnauthorizedException(status, apiError);
            case 404 -> new NotFoundException(status, apiError);
            default -> new ServerErrorException(status, apiError);
        };
    }

    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            JsonMapper.Builder builder = JsonMapper.builder().addModule(new JavaTimeModule());
            builder.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.enable(SerializationFeature.INDENT_OUTPUT);
            builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            objectMapper = builder.build();
        }
        return objectMapper;
    }

    private void updateId() {
        this.id = Hashing.hash(uri.toASCIIString().toLowerCase());
    }

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json");


}
