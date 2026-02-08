package net.microfalx.bootstrap.cloud.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoogleIdentityService implements InitializingBean {

    @Autowired private GoogleProperties properties;

    private GoogleIdTokenVerifier verifier;

    public GoogleIdToken.Payload verify(String idTokenString) throws Exception {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid ID token");
        }
        return idToken.getPayload();
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
