package net.microfalx.bootstrap.restapi.client;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.microfalx.lang.FileUtils.getFileName;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.UriUtils.SLASH;

/**
 * Holds information about a REST client audit entry.
 */
@Data
public class RestApiAudit {

    private RestClient client;
    private String name;
    private String requestId;
    private String requestMethod;
    private String requestPath;
    private String requestQuery;
    private int responseStatus;
    private int responseLength;
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Duration duration;

    public String getName() {
        if (isEmpty(name)) {
            name = defaultIfEmpty(capitalizeWords(getFileName(requestPath)), SLASH);
        }
        return name;
    }

    public String getRequestPath() {
        return defaultIfEmpty(requestPath, SLASH);
    }

    public boolean isSuccess() {
        return responseStatus >= 200 && responseStatus < 300;
    }

    public Duration getDuration() {
        if (duration == null && startedAt != null && endedAt != null) {
            return Duration.between(startedAt, endedAt);
        }
        return duration;
    }
}
