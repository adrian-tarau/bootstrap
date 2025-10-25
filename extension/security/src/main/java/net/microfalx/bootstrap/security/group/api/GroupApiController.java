package net.microfalx.bootstrap.security.group.api;

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
import net.microfalx.bootstrap.security.group.GroupService;
import net.microfalx.bootstrap.security.group.jpa.Group;
import net.microfalx.bootstrap.security.group.jpa.GroupRepository;
import net.microfalx.bootstrap.security.user.api.UserDto;
import net.microfalx.bootstrap.security.user.jpa.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@DataSet(model = Group.class, timeFilter = false)
@Tag(name = "Groups", description = "Group Management API")
@Transactional
public class GroupApiController extends RestApiDataSetController<Group, GroupDTO, Long> {

    @Autowired
    private GroupRepository userRepository;

    @Autowired
    private GroupService groupService;

    @Operation(summary = "List groups", description = "Returns a list of groups with search and paging.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GroupDTO.class)))
    @Transactional(readOnly = true)
    @GetMapping
    public List<GroupDTO> list(
            @Parameter(description = "The query used to filter by various model fields", name = "name")
            @RequestParam(name = "query", required = false) String query,

            @Parameter(description = "The sorting desired for the result set", name = "name=asc")
            @RequestParam(name = "sort", required = false) String sort,

            @Parameter(description = "The page to return for the result set", example = "0")
            @RequestParam(name = "page", required = false) int page,

            @Parameter(description = "The page size for the result set", example = "20")
            @RequestParam(name = "page-size", required = false) int pageSize
    ) {
        return doList(null, query, sort, page, pageSize);
    }

    @Operation(summary = "Get group", description = "Returns a single group by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GroupDTO.class)))
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public GroupDTO get(@Parameter(description = "The user identifier", example = "42") @PathVariable Long id) {
        return doFind(id);
    }

    @Operation(summary = "Create group")
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = GroupDTO.class)))
    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public GroupDTO create(@RequestBody GroupDTO group) {
        doCreate(group);
        return group;
    }

    @Operation(summary = "Update group")
    @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = GroupDTO.class)))
    @PutMapping("/{id}")
    public GroupDTO update(@Parameter(description = "The group identifier", example = "42") @PathVariable Long id, @RequestBody GroupDTO group) {
        doUpdate(group);
        return group;
    }

    @Operation(summary = "Delete group")
    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "No Content")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "The group identifier", example = "42") @PathVariable Long id) {
        doDeleteById(id);
    }

    @Operation(summary = "Add member", description = "Add a user to this group.")
    @ApiResponse(responseCode = "200", description = "Member added")
    @PostMapping("/{groupId}/members/{userId}")
    public void addMember(@PathVariable int groupId, @PathVariable("userId") String username) {
        userRepository.addUser(groupId, username);
    }

    @Operation(summary = "Remove member", description = "Remove a user from this group.")
    @ApiResponse(responseCode = "204", description = "Member removed")
    @DeleteMapping("/{groupId}/members/{userId}")
    public void removeMember(@PathVariable int groupId, @PathVariable("userId") String username) {
        userRepository.removeUser(groupId, username);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Group, Field<Group>, Long> dataSet, Group model, State state) {
        groupService.setRoles(model, model.getRoles());
    }


    @Override
    protected Class<? extends RestApiMapper<Group, GroupDTO>> getMapperClass() {
        return GroupMapper.class;
    }
}
