package net.microfalx.bootstrap.security.group.api;

import net.microfalx.bootstrap.restapi.AbstractRestApiMapper;
import net.microfalx.bootstrap.security.group.jpa.Group;
import net.microfalx.bootstrap.security.user.api.UserDto;
import net.microfalx.bootstrap.security.user.jpa.User;
import org.modelmapper.ModelMapper;

public class GroupMapper extends AbstractRestApiMapper<Group, GroupDTO> {

    @Override
    protected void configureMapper(ModelMapper modelMapper) {
        modelMapper.getConfiguration().setCollectionsMergeEnabled(false);
    }

}
