package net.microfalx.bootstrap.restapi.client;

import lombok.Data;
import net.microfalx.lang.IdGenerator;
import net.microfalx.lang.StringUtils;

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
    private String requestId = REQUEST_ID.get();
    private String requestMethod;
    private String requestPath;
    private String requestQuery;
    private int responseStatus;
    private int responseLength;
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Duration duration;

    private static final IdGenerator ID_GENERATOR = IdGenerator.get("RestAPIClient");
    static final ThreadLocal<String> CURRENT_REQUEST_PATTERN = new ThreadLocal<>();
    static final ThreadLocal<String> REQUEST_ID = ThreadLocal.withInitial(ID_GENERATOR::nextAsString);

    public String getName() {
        if (isEmpty(name)) {
            name = defaultIfEmpty(capitalizeWords(getFileName(requestPath)), SLASH);
        }
        return name;
    }

    public String getRequestPath() {
        return addStartSlash(defaultIfEmpty(requestPath, SLASH));
    }

    public String getRequestPattern() {
        String pattern = CURRENT_REQUEST_PATTERN.get();
        return addStartSlash(defaultIfEmpty(pattern, getRootPath(getRequestPath())));
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

    private static String getRootPath(String path) {
        return StringUtils.split(path, "/")[0];
    }
}
