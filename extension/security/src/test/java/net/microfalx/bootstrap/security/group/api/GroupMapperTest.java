package net.microfalx.bootstrap.security.group.api;

import net.microfalx.bootstrap.restapi.AbstractRestApiMapper;
import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.bootstrap.security.group.jpa.Group;
import net.microfalx.bootstrap.security.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GroupMapperTest {

    private RestApiMapper<Group, GroupDTO> mapper;

    @BeforeEach
    void setUp() {
        mapper = new GroupMapper();
    }

    @Test
    void toEntity() {
        GroupDTO groupDTO = createGroupDTO();
        Group group = mapper.toEntity(groupDTO);
        assertEquals(groupDTO.getName(), group.getName());
        assertEquals(groupDTO.getDescription(), group.getDescription());
        assertEquals(groupDTO.isEnabled(), group.isEnabled());
        assertIterableEquals(groupDTO.getRoles(), group.getRoles());
    }

    @Test
    void toDTO() {
        Group group = createGroup();
        GroupDTO groupDTO = mapper.toDto(group);
        assertEquals(group.getName(), groupDTO.getName());
        assertEquals(group.getDescription(), groupDTO.getDescription());
        assertEquals(group.isEnabled(), groupDTO.isEnabled());
        assertIterableEquals(group.getRoles(), groupDTO.getRoles());
    }

    private GroupDTO createGroupDTO() {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setRoles(Set.of(Role.ADMIN, Role.create("12345").build()));
        groupDTO.setEnabled(true);
        groupDTO.setUsers(Collections.emptyList());
        groupDTO.setName("Test Group");
        groupDTO.setDescription("A group for testing");
        return groupDTO;
    }

    private Group createGroup() {
        Group group = new Group();
        group.setRoles(Set.of(Role.GUEST, Role.create("67890").build()));
        group.setName("Sample Group");
        group.setUsers(Collections.emptyList());
        group.setEnabled(true);
        group.setDescription("A sample group for testing");
        return group;
    }
}