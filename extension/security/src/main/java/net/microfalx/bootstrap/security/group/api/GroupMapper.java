package net.microfalx.bootstrap.security.group.api;

import net.microfalx.bootstrap.restapi.AbstractRestApiMapper;
import net.microfalx.bootstrap.security.group.jpa.Group;
import net.microfalx.bootstrap.security.user.Role;
import net.microfalx.lang.NamedIdentityAware;

import java.util.stream.Collectors;

public class GroupMapper extends AbstractRestApiMapper<Group, GroupDTO> {

    @Override
    protected GroupDTO doToDto(Group group) {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setName(group.getName());
        groupDTO.setEnabled(group.isEnabled());
        groupDTO.setDescription(group.getDescription());
        groupDTO.setId(group.getId());
        groupDTO.setRoles(group.getRoles().stream().map(NamedIdentityAware::getName).collect(Collectors.toSet()));
        return groupDTO;
    }

    @Override
    protected Group doToEntity(GroupDTO groupDTO) {
        Group group = new Group();
        group.setEnabled(groupDTO.isEnabled());
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());
        group.setId(groupDTO.getId());
        group.setRoles(groupDTO.getRoles().stream().map(n ->
                (Role) Role.create(n).name(n).build()).collect(Collectors.toSet()));
        return group;
    }
}
