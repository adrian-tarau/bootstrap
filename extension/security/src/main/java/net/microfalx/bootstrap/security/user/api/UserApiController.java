package net.microfalx.bootstrap.security.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.FailedApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.restapi.RestApiDataSetController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@DataSet(model = net.microfalx.bootstrap.security.user.jpa.User.class, timeFilter = false)
@FailedApiResponse(ref = "api_error")
@Tag(name = "Users", description = "User Management API")
public class UserApiController extends RestApiDataSetController<UserDto, String> {

    @Operation(summary = "List Users", description = "Returns users with optional search and paging.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public List<UserDto> list(@Parameter(description = "Search by display name or email", example = "jane")
                           @RequestParam(required = false) String q,
                              @Parameter(description = "Page number (0-based)", example = "0")
                           @RequestParam(defaultValue = "0") int page,
                              @Parameter(description = "Page size", example = "20")
                           @RequestParam(defaultValue = "20") int size) {
        return List.of();
    }

    @Operation(summary = "Get a user", description = "Returns a single user by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UserDto.class)))
    @GetMapping("/{id}")
    public UserDto get(@Parameter(description = "The user identifier", example = "42") @PathVariable Long id) {
        return null;
    }

    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = UserDto.class)))
    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto user) {
        return user;
    }

    @Operation(summary = "Update a user")
    @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = UserDto.class)))
    @PutMapping("/{id}")
    public UserDto update(@Parameter(description = "The user identifier", example = "42") @PathVariable Long id, @RequestBody UserDto user) {
        return null;
    }

    @Operation(summary = "Delete a user")
    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "No Content")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "The user identifier", example = "42") @PathVariable Long id) {

    }
}
