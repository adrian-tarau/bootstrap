package net.microfalx.bootstrap.restapi.client;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import static net.microfalx.lang.StringUtils.isNotEmpty;

public class RestClientApiKeyInterceptor implements Interceptor {

    private static final String DEFAULT_HEADER_NAME = "X-API-Key";

    static ThreadLocal<String> API_KEY = new ThreadLocal<>();
    static ThreadLocal<Boolean> USE_HEADER = ThreadLocal.withInitial(() -> Boolean.TRUE);

    @Override
    public Response intercept(Chain chain) throws IOException {
        String apiKey = API_KEY.get();
        if (isNotEmpty(apiKey)) {
            Request.Builder builder = chain.request().newBuilder();
            if (USE_HEADER.get()) {
                builder = builder.header(DEFAULT_HEADER_NAME, apiKey);
            } else {
                builder = builder.header("Authorization", "Bearer " + apiKey);
            }
            return chain.proceed(builder.build());
        } else {
            return chain.proceed(chain.request());
        }
    }
}
