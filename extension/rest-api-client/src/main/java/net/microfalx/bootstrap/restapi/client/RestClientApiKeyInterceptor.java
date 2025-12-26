package net.microfalx.bootstrap.restapi.client;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * An OkHttp interceptor which adds an API key to the request headers.
 */
class RestClientApiKeyInterceptor implements Interceptor {

    static ThreadLocal<RestClient> CLIENT = new ThreadLocal<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        RestClient restClient = CLIENT.get();
        String apiKey = restClient.getApiKey();
        if (isNotEmpty(apiKey)) {
            Request.Builder builder = chain.request().newBuilder();
            if (restClient.isUseHeader()) {
                builder = builder.header(restClient.getApiKeyHeaderName(), apiKey);
            } else {
                builder = builder.header("Authorization", "Bearer " + apiKey);
            }
            return chain.proceed(builder.build());
        } else {
            return chain.proceed(chain.request());
        }
    }
}
