package net.microfalx.bootstrap.restapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Schema(name = "Error", description = "THe error payload")
@Getter
@Setter
@ToString
public class RestApiError {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(example = "USER_NOT_FOUND") String code;
    @Schema(example = "User 42 not found") String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(example = "{ \"email\": \"Invalid email format\", \"password\": \"Password is too short\" }")
    Map<String, String> errors;
}
