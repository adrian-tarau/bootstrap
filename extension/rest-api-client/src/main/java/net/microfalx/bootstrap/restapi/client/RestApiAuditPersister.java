package net.microfalx.bootstrap.restapi.client;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.restapi.client.jpa.Audit;
import net.microfalx.bootstrap.restapi.client.jpa.AuditRepository;
import net.microfalx.bootstrap.restapi.client.jpa.Client;
import net.microfalx.bootstrap.restapi.client.jpa.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RestApiAuditPersister {

    @Autowired private AuditRepository auditRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private MetadataService metadataService;
    private NaturalIdEntityUpdater<Client, Integer> clientUpdater;

    void init() {
        clientUpdater = new NaturalIdEntityUpdater<>(metadataService, clientRepository);
    }

    void persistAudit(RestApiAudit apiAudit) {
        Audit audit = createAudit(apiAudit);
        try {
            auditRepository.save(audit);
        } catch (Exception e) {
            LOGGER.atWarn().setCause(e).log("Failed to persist audit for {}. request path {}",
                    apiAudit.getClient().getName(), audit.getRequestPath());
        }
    }

    private Audit createAudit(RestApiAudit apiAudit) {
        Audit audit = new Audit();
        audit.setClient(persistClient(apiAudit.getClient()));
        audit.setName(apiAudit.getName());
        audit.setHttpMethod(apiAudit.getRequestMethod());
        audit.setHttpStatus(apiAudit.getResponseStatus());
        audit.setQueryParams(apiAudit.getRequestQuery());
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
        return clientUpdater.findByNaturalIdOrCreate(client);
    }

}
