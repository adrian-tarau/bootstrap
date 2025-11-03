package net.microfalx.bootstrap.security.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.restapi.RestApiDataSetController;
import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.bootstrap.security.user.jpa.User;
import net.microfalx.bootstrap.security.user.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@DataSet(model = net.microfalx.bootstrap.security.user.jpa.User.class, timeFilter = false)
@Tag(name = "Users", description = "User Management API")
public class UserApiController extends RestApiDataSetController<User, UserDto, Long> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "List users", description = "Returns a list of users with search and paging.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UserDto.class)))
    @GetMapping
    public List<UserDto> list(
            @Parameter(description = "The query used to filter by various model fields", name = "query", example = "username")
            @RequestParam(name = "query", required = false) String query,

            @Parameter(description = "The sorting desired for the result set", name = "sort", example = "modifiedAt=desc")
            @RequestParam(name = "sort", required = false) String sort,

            @Parameter(description = "The page to return for the result set", name = "page", example = "0")
            @RequestParam(name = "page", required = false) int page,

            @Parameter(description = "The page size for the result set", name = "page size", example = "20")
            @RequestParam(name = "page-size", required = false) int pageSize
    ) {
        return doList(null, query, sort, page, pageSize);
    }

    @Operation(summary = "Get user", description = "Returns a single user by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UserDto.class)))
    @GetMapping("/{id}")
    public UserDto get(@Parameter(description = "The user identifier", example = "42") @PathVariable Long id) {
        return doFind(id);
    }

    @Operation(summary = "Create user")
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = UserDto.class)))
    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto user) {
        doCreate(user);
        return user;
    }

    @Operation(summary = "Update user")
    @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = UserDto.class)))
    @PutMapping("/{id}")
    public UserDto update(@Parameter(description = "The user identifier", example = "42") @PathVariable Long id, @RequestBody UserDto user) {
        doUpdate(user);
        return user;
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "No Content")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "The user identifier", example = "42") @PathVariable Long id) {
        doDeleteById(id);
    }

    @Operation(summary = "Add user to group", description = "Add a user to a group.")
    @ApiResponse(responseCode = "204", description = "User updated with new group")
    @PostMapping("/{userId}/groups/{groupId}")
    public void addUserToGroup(@PathVariable("userId") String userName, @PathVariable int groupId) {
        try{
            userRepository.addUserToGroup(userName, groupId);
        }catch (Exception e){
            // the user is probably already in the group, ignore
        }
    }

    @Operation(summary = "Remove user from group", description = "Remove a user from a group.")
    @ApiResponse(responseCode = "204", description = "User updated without the group")
    @DeleteMapping("/{userId}/groups/{groupId}")
    public void removeUserFromGroup(@PathVariable("userId") String userName, @PathVariable int groupId) {
        userRepository.removeUserToGroup(userName, groupId);
    }

    @Override
    protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<User, Field<User>, Long> dataSet, User model, State state) {
        super.beforePersist(dataSet, model, state);
        if (state == State.ADD) model.setPassword(passwordEncoder.encode(model.getPassword()));
    }

    @Override
    protected Class<? extends RestApiMapper<User, UserDto>> getMapperClass() {
        return UserMapper.class;
    }
}
