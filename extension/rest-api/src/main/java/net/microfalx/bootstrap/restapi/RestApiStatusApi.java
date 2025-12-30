package net.microfalx.bootstrap.restapi;

import retrofit2.http.GET;

/**
 * Retrofit API interface for status endpoint.
 */
public interface RestApiStatusApi {

    /**
     * Returns the simple status.
     *
     * @return the status string
     */
    @GET("status")
    String status();
}
