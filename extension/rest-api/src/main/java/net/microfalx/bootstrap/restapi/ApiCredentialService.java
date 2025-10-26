package net.microfalx.bootstrap.restapi;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * A service which can identify a <code>user</code> based on an API Key or Bearer token.
 */
public interface ApiCredentialService {

    /**
     * Authenticates a user based on a token.
     *
     * @param token the token
     * @return the user, null if no such token exists
     */
    UserDetails authenticateBearer(String token);

    /**
     * Authenticates a user based on a API key.
     *
     * @param apiKey the api key
     * @return the user, null if no such key exists
     */
    UserDetails authenticateApiKey(String apiKey);
}
