package net.microfalx.bootstrap.restapi.client;

import lombok.Data;
import net.microfalx.lang.FileUtils;
import net.microfalx.lang.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.microfalx.lang.StringUtils.isEmpty;

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
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Duration duration;

    public String getName() {
        if (isEmpty(name)) {
            name = StringUtils.capitalizeWords(FileUtils.getFileName(requestQuery));
        }
        return name;
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
