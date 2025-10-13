package net.microfalx.bootstrap.security.user.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(name = "User", description = "A security user")
public class UserDto implements net.microfalx.bootstrap.security.user.User {

    private String userName;
    private String name;
    private String password;
    private String email;
    private boolean enabled;
    private String description;

    @Override
    public String getId() {
        return getUserName();
    }
}
