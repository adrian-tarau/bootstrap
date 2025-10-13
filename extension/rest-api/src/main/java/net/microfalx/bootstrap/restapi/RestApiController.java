package net.microfalx.bootstrap.restapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Base class for all REST API controllers.
 */

@SecurityRequirement(name = "bearer")
@SecurityRequirement(name = "basicAuth")
@SecurityRequirement(name = "apiKeyAuth")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = RestApiError.class)))
@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content(schema = @Schema(implementation = RestApiError.class)))
public abstract class RestApiController {
}
