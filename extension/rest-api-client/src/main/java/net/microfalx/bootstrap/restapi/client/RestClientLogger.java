package net.microfalx.bootstrap.restapi.client;

import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Slf4j
class RestClientLogger {

    private final HttpLoggingInterceptor.Level level;

    RestClientLogger(HttpLoggingInterceptor.Level level) {
        requireNonNull(level);
        this.level = level;
    }

    HttpLoggingInterceptor create() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(this::log);
        logging.setLevel(level);
        return logging;
    }

    private void log(String message) {
        if (level == HttpLoggingInterceptor.Level.BODY) {
            LOGGER.trace(message);
        } else {
            LOGGER.debug(message);
        }
    }
}
