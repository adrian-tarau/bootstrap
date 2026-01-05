package net.microfalx.bootstrap.restapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.FailedApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Status", description = "A collection of endpoints to check the status of the REST API")
@RequestMapping(value = "/api/v1/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@FailedApiResponse(ref = "api_error")
@RestController
@PermitAll
public class RestApiStatusController extends RestApiController {

    @GetMapping
    @Operation(summary = "Get status", description = "Returns a single word 'OK' if the service is up and running")
    @ApiResponse(responseCode = "200", description = "OK")
    public String status() {
        return "OK";
    }
}
