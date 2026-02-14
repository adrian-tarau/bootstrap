package net.microfalx.bootstrap.restapi.client;

import net.microfalx.lang.ExceptionUtils;
import okhttp3.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.microfalx.bootstrap.restapi.client.RestApiAudit.REQUEST_ID;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;

public class RestApiAuditInterceptor implements Interceptor {

    private final RestClientService restClientService;

    public RestApiAuditInterceptor(RestClientService restClientService) {
        this.restClientService = restClientService;
    }

    @Override
    public Response intercept(Chain chain) {
        Request request = chain.request();
        LocalDateTime start = LocalDateTime.now();
        RestApiAudit audit = createAudit(request, start);
        long startNs = System.nanoTime();
        try {
            restClientService.auditStart(audit);
            Response response = chain.proceed(request);
            updateAudit(audit, response);
            return response;
        } catch (Exception e) {
            updateAudit(audit, e);
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        } finally {
            completeAudit(audit, startNs);
        }
    }

    private RestApiAudit createAudit(Request request, LocalDateTime start) {
        HttpUrl url = request.url();
        return new RestApiAudit().setStartedAt(start).setClient(RestClient.current())
                .setRequestMethod(request.method()).setRequestQuery(url.query())
                .setRequestPath(url.encodedPath()).setResponseLength(-1);
    }

    private void updateAudit(RestApiAudit audit, Response response) {
        audit.setResponseStatus(response.code());
        ResponseBody body = response.body();
        if (body == null) return;
        audit.setResponseLength((int) body.contentLength());
    }

    private void completeAudit(RestApiAudit audit, long startNs) {
        long durationNs = System.nanoTime() - startNs;
        audit.setEndedAt(LocalDateTime.now());
        audit.setDuration(Duration.ofNanos(durationNs));
        restClientService.auditEnd(audit);
        REQUEST_ID.remove();
    }

    private void updateAudit(RestApiAudit audit, Exception exception) {
        audit.setResponseStatus(500);
        audit.setErrorMessage("Internal error in interceptor, root cause: " + getRootCauseDescription(exception));
    }
}
