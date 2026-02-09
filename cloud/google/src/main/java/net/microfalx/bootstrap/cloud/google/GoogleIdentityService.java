package net.microfalx.bootstrap.cloud.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import net.microfalx.lang.ArgumentUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleIdentityService implements InitializingBean {

    @Autowired private GoogleProperties properties;

    private GoogleIdTokenVerifier verifier;

    /**
     * Verifies the given ID token string and returns the corresponding GoogleIdToken if valid.
     *
     * @param idTokenString the ID token string to verify
     * @return the GoogleIdToken if the token is valid
     * @throws AuthenticationException if there is a security issue during verification
     */
    public GoogleIdToken verify(String idTokenString) throws AuthenticationException {
        ArgumentUtils.requireNotEmpty(idTokenString);
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new BadCredentialsException("Invalid Google ID token");
            }
            return idToken;
        } catch (GeneralSecurityException e) {
            throw new BadCredentialsException("Invalid Google ID token", e);
        } catch (IOException e) {
            throw new AuthenticationServiceException("I/O error verifying Google ID token", e);
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeVerifier();
    }

    private void initializeVerifier() {
        if (!properties.isGisEnabled()) return;
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(properties.getClientId()))
                .build();
    }
}
