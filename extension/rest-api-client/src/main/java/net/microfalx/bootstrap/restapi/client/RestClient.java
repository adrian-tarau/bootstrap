package net.microfalx.bootstrap.restapi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import net.microfalx.bootstrap.restapi.client.exception.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A REST client which can be used to create API wrappers.
 * <p>
 * Behind the scenes, it uses <a href="https://square.github.io/retrofit/">Retrofit</a> and
 * <a href="https://square.github.io/okhttp/">OkHttp</a>.
 */
@RequiredArgsConstructor
public class RestClient {

    private final OkHttpClient client;
    private ObjectMapper objectMapper;

    private URI uri;
    private Retrofit retrofit;

    private String apiKey;

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
     * Return the Retrofit instance used to create API wrappers.
     *
     * @return a non-null instance
     */
    public Retrofit getWrapper() {
        if (retrofit == null) {
            try {
                Retrofit.Builder builder = new Retrofit.Builder().baseUrl(uri.toURL()).client(client);
                builder.addConverterFactory(JacksonConverterFactory.create(getObjectMapper()));
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
    public <T> T execute(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (!response.isSuccessful()) throw map(response);
            return response.body();
        } catch (IOException e) {
            throw new ServerErrorException(500, new ApiError().setStatus(500).setMessage(e.getMessage()));
        }
    }

    /**
     * Creates an API wrapper for the given API interface.
     *
     * @param apiClass the API interface class
     * @param <A>      the API type
     * @return the API wrapper
     * @see #execute(Call)
     */
    public <A> A create(Class<A> apiClass) {
        return getWrapper().create(apiClass);
    }

    private ApiException map(retrofit2.Response<?> response) {
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
            case 400 -> new BadRequestException(status, apiError);
            case 401, 403 -> new UnauthorizedException(status, apiError);
            case 404 -> new NotFoundException(status, apiError);
            default -> new ServerErrorException(status, apiError);
        };
    }

    public ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            JsonMapper.Builder builder = JsonMapper.builder().addModule(new JavaTimeModule());
            builder.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper = builder.build();
        }
        return objectMapper;
    }

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json");


}
