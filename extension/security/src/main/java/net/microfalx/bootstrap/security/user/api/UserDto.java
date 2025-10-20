package net.microfalx.bootstrap.security.user.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.Identifiable;

@Getter
@Setter
@ToString
@Schema(name = "User", description = "A security user")
public class UserDto implements Identifiable<String> {

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
