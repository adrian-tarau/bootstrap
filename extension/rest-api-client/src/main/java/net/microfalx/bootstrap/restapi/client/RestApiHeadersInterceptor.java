package net.microfalx.bootstrap.restapi.client;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * An OkHttp interceptor which adds an API key and other headers
 * to the request headers.
 */
public class RestApiHeadersInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        RestClient restClient = RestClient.current();
        if (restClient == null) {
            throw new IllegalStateException("A RestClient is not attached to the current thread: "
                    + Thread.currentThread().getName());
        }
        Request.Builder builder = chain.request().newBuilder();
        builder.header("Content-Type", "application/json; charset=utf-8");
        builder.header("Accept", "application/json");
        builder.header("Cache-Control", "no-cache");
        builder.header("X-Request-ID", RestApiAudit.REQUEST_ID.get());
        String apiKey = restClient.getApiKey();
        if (isNotEmpty(apiKey)) {
            if (restClient.isUseHeader()) {
                builder = builder.header(restClient.getApiKeyHeaderName(), apiKey);
            } else {
                builder = builder.header("Authorization", "Bearer " + apiKey);
            }
        }
        return chain.proceed(builder.build());
    }
}
