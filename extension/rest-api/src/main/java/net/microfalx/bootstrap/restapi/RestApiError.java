package net.microfalx.bootstrap.restapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

@Schema(name = "Error", description = "THe error payload")
@Getter
@Setter
@ToString
public class RestApiError {

    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;

    @Schema(description = "The timestamp when the error occurred")
    private Instant timestamp;

    @Schema(example = "400", description = "The HTTP status code")
    private int status;

    @Schema(example = "/api/v1/users", description = "The request path which caused the error")
    private String path;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(example = "USER_NOT_FOUND", description = "A machine-readable error code")
    private String code;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(example = "User 42 not found", description = "A human-readable error message")
    private String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(example = "Authentication is required to access this resource", description = "A human-readable description of the error message")
    private String description;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(example = "{ \"email\": \"Invalid email format\", \"password\": \"Password is too short\" }",
            description = "An optional map of validation errors where the key is the field name and the value is the error message")
    private Map<String, Object> details;

    public RestApiError(int status, String path) {
        this(status, path, Instant.now());
    }

    public RestApiError(int status, String path, Instant timestamp) {
        requireNonNull(path);
        requireNotEmpty(timestamp);
        this.status = status;
        this.path = path;
        this.timestamp = timestamp;
    }
}
