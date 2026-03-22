package net.microfalx.bootstrap.restapi.client.exception;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

/**
 * A class representing an API error response.
 */
@Data
public class ApiError {

    private int httpStatus;
    private String status;
    @JsonAlias("code")
    private String errorCode;
    private String message;
    @JsonAlias("details")
    private Map<String, Object> errors = Collections.emptyMap();
}
