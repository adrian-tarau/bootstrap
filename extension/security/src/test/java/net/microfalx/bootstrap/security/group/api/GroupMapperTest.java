package net.microfalx.bootstrap.security.group.api;

import net.microfalx.bootstrap.restapi.AbstractRestApiMapper;
import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.bootstrap.security.group.jpa.Group;
import net.microfalx.bootstrap.security.user.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GroupMapperTest {

    private GroupMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GroupMapper();
        mapper.afterPropertiesSet();
    }

    @Test
    void toEntity() {
        GroupDTO groupDTO = createGroupDTO();
        Group group = mapper.toEntity(groupDTO);
        assertEquals(groupDTO.getId(), group.getId());
        assertEquals(groupDTO.getName(), group.getName());
        assertEquals(groupDTO.getDescription(), group.getDescription());
        assertEquals(groupDTO.isEnabled(), group.isEnabled());
        assertThat(group.getRoles()).containsExactlyInAnyOrder((Role) Role.create("Admin").name("Admin").build(), (Role) Role.create("12345").name("12345").build());
    }

    @Test
    void toDTO() {
        Group group = createGroup();
        GroupDTO groupDTO = mapper.toDto(group);
        assertEquals(group.getId(), groupDTO.getId());
        assertEquals(group.getName(), groupDTO.getName());
        assertEquals(group.getDescription(), groupDTO.getDescription());
        assertEquals(group.isEnabled(), groupDTO.isEnabled());
        assertThat(groupDTO.getRoles()).containsExactlyInAnyOrder("Guest", "67890");
    }

    private GroupDTO createGroupDTO() {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setRoles(Set.of("Admin", "12345"));
        groupDTO.setEnabled(true);
        groupDTO.setId(1);
        groupDTO.setName("Test Group");
        groupDTO.setDescription("A group for testing");
        return groupDTO;
    }

    private Group createGroup() {
        Group group = new Group();
        group.setRoles(Set.of(Role.GUEST, (Role) Role.create("67890").name("67890").build()));
        group.setName("Sample Group");
        group.setUsers(Collections.emptyList());
        group.setEnabled(true);
        group.setId(1);
        group.setDescription("A sample group for testing");
        return group;
    }
}