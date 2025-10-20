package net.microfalx.bootstrap.security.group.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
@Schema(name = "Group", description = "A security group")
public class GroupDTO {

    private int id;
    private String name;
    private boolean enabled;
    private Set<String> roles;
    private String description;

}
