package net.microfalx.bootstrap.restapi.client;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

class RestClientRetryInterceptor implements Interceptor {

    private final int maxRetries;

    RestClientRetryInterceptor(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        IOException lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Response response = chain.proceed(request);
                if (response.isSuccessful() || response.code() < 500) return response;
                response.close();
            } catch (IOException e) {
                lastException = e;
            }
            try {
                Thread.sleep(1000L * attempt); // simple backoff
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        throw lastException != null
                ? lastException
                : new IOException("Request failed after retries");
    }
}
