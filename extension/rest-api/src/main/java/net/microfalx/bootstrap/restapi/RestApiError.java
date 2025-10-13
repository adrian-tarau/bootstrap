package net.microfalx.bootstrap.restapi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(name = "Error", description = "THe error payload")
@Getter
@Setter
@ToString
public class RestApiError {
    @Schema(example = "USER_NOT_FOUND") String code;
    @Schema(example = "User 42 not found") String message;
}
