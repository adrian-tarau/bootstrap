package net.microfalx.bootstrap.security.user.api;

import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.bootstrap.security.user.jpa.User;

public interface UserMapper extends RestApiMapper<User, UserDto> {
}
