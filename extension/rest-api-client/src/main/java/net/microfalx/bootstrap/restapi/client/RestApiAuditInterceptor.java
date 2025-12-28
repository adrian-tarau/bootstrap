package net.microfalx.bootstrap.restapi.client;

import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.IdGenerator;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.time.Duration;
import java.time.LocalDateTime;

public class RestApiAuditInterceptor implements Interceptor {

    private final RestClientService restClientService;
    private static final IdGenerator ID_GENERATOR = IdGenerator.get("rest_api_client_audit");

    public RestApiAuditInterceptor(RestClientService restClientService) {
        this.restClientService = restClientService;
    }

    @Override
    public Response intercept(Chain chain) {
        Request request = chain.request();
        LocalDateTime start = LocalDateTime.now();
        HttpUrl url = request.url();
        RestApiAudit audit = new RestApiAudit().setRequestId(ID_GENERATOR.nextAsString()).setStartedAt(start)
                .setClient(RestClient.CLIENT.get());
        audit.setRequestMethod(request.method());
        audit.setRequestQuery(url.query());
        audit.setRequestPath(url.encodedPath());
        long startNs = System.nanoTime();
        try {
            restClientService.auditStart(audit);
            Response response = chain.proceed(request);
            audit.setResponseStatus(response.code());
            return response;
        } catch (Exception e) {
            audit.setResponseStatus(500);
            audit.setErrorMessage(ExceptionUtils.getRootCauseMessage(e));
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        } finally {
            long durationNs = System.nanoTime() - startNs;
            audit.setEndedAt(LocalDateTime.now());
            audit.setDuration(Duration.ofNanos(durationNs));
            restClientService.auditEnd(audit);
        }
    }
}
