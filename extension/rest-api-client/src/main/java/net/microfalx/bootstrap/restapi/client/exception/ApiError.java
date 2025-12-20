package net.microfalx.bootstrap.restapi.client.exception;

import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public class ApiError {

    private int status;
    private String errorCode;
    private String message;
    private Map<String, Object> details = Collections.emptyMap();
}
