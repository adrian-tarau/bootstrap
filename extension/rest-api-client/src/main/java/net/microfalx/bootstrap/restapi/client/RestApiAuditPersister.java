package net.microfalx.bootstrap.restapi.client;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.restapi.client.jpa.Audit;
import net.microfalx.bootstrap.restapi.client.jpa.AuditRepository;
import net.microfalx.bootstrap.restapi.client.jpa.Client;
import net.microfalx.bootstrap.restapi.client.jpa.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Component
@Slf4j
public class RestApiAuditPersister {

    @Autowired private AuditRepository auditRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private MetadataService metadataService;
    private NaturalIdEntityUpdater<Client, Integer> clientUpdater;
    private TextEncryptor textEncryptor;

    void init(TextEncryptor textEncryptor) {
        clientUpdater = new NaturalIdEntityUpdater<>(metadataService, clientRepository);
        clientUpdater.setUpdatable("apiKey", false);
        this.textEncryptor = textEncryptor;
    }

    String getApiKey(RestClient restClient) {
        requireNonNull(restClient);
        Optional<Client> client = clientRepository.findByNaturalId(restClient.getId());
        String apiKey = client.map(c -> c.getApiKey()).orElse(null);
        if (apiKey != null) apiKey = textEncryptor.decrypt(apiKey);
        return apiKey;
    }

    void persistAudit(RestApiAudit apiAudit) {
        try {
            Audit audit = createAudit(apiAudit);
            auditRepository.save(audit);
        } catch (Exception e) {
            LOGGER.atWarn().setCause(e).log("Failed to persist audit for {}. request path {}",
                    apiAudit.getClient().getName(), apiAudit.getRequestPath());
        }
    }

    private Audit createAudit(RestApiAudit apiAudit) {
        Audit audit = new Audit();
        audit.setClient(persistClient(apiAudit.getClient()));
        audit.setName(apiAudit.getName());
        audit.setRequestMethod(apiAudit.getRequestMethod());
        audit.setRequestPath(apiAudit.getRequestPath());
        audit.setRequestQuery(apiAudit.getRequestQuery());
        audit.setResponseStatus(apiAudit.getResponseStatus());
        audit.setResponseLength(apiAudit.getResponseLength());
        audit.setErrorMessage(apiAudit.getErrorMessage());
        audit.setSuccess(apiAudit.isSuccess());
        audit.setStartedAt(apiAudit.getStartedAt());
        audit.setEndedAt(apiAudit.getEndedAt());
        audit.setDuration((int) (apiAudit.getDuration().toNanos() / 1_000L));
        return audit;
    }

    private Client persistClient(RestClient restClient) {
        Client client = new Client();
        client.setNaturalId(restClient.getId());
        client.setName(restClient.getName());
        client.setDescription(restClient.getDescription());
        client.setUri(restClient.getUri().toASCIIString());
        client.setApiKey(textEncryptor.encrypt(restClient.getApiKey()));
        return clientUpdater.findByNaturalIdOrCreate(client);
    }

}
