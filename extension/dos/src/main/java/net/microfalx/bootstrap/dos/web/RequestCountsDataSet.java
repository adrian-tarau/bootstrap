package net.microfalx.bootstrap.dos.web;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.dos.DosService;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.lang.annotation.Provider;

@Provider
public class RequestCountsDataSet extends MemoryDataSet<RequestCounts, Field<RequestCounts>, String> {

    public RequestCountsDataSet(DataSetFactory<RequestCounts, Field<RequestCounts>, String> factory, Metadata<RequestCounts, Field<RequestCounts>, String> metadata) {
        super(factory, metadata);
        expireImmediately();
    }

    @Override
    protected Iterable<RequestCounts> extractModels(Filter filterable) {
        return getService(DosService.class).getRequestCounts().stream().map(this::map).toList();
    }

    private RequestCounts map(net.microfalx.bootstrap.dos.RequestCounts requestCounts) {
        RequestCounts model = new RequestCounts();
        model.setId(requestCounts.getId());
        model.setName(requestCounts.getName());
        model.setIp(requestCounts.getIp());
        model.setCanonicalHostName(requestCounts.getCanonicalHostName());

        model.setAccessCount(requestCounts.getAccessCount());
        model.setNotFoundCount(requestCounts.getNotFoundCount());
        model.setFailureCount(requestCounts.getFailureCount());
        model.setInvalidCount(requestCounts.getInvalidCount());
        model.setValidationCount(requestCounts.getValidationCount());
        model.setSecurityCount(requestCounts.getSecurityCount());
        model.setThroughput(requestCounts.getThroughput());

        model.setLocation(requestCounts.getLocation().getDescription());

        model.setCreatedAt(requestCounts.getCreatedAt());
        model.setModifiedAt(requestCounts.getModifiedAt());

        return model;
    }
}
