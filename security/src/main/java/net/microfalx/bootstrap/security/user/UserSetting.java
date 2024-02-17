package net.microfalx.bootstrap.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import net.microfalx.bootstrap.jdbc.entity.TimestampAware;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "security_users_settings")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
@IdClass(UserSetting.Id.class)
public class UserSetting extends TimestampAware {

    @jakarta.persistence.Id
    @Column(name = "username", nullable = false)
    @NotBlank
    @EqualsAndHashCode.Include
    private String userName;

    @jakarta.persistence.Id
    @Column(name = "name", nullable = false)
    @NotBlank
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "value")
    private byte[] value;

    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {

        @Serial
        private static final long serialVersionUID = -2435868033684749059L;

        private String userName;
        private String name;

    }
}
