package net.microfalx.bootstrap.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import net.microfalx.bootstrap.jdbc.entity.surrogate.TimestampAware;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "security_users_settings")
@Getter
@Setter
@ToString
@IdClass(UserSetting.Id.class)
public class UserSetting extends TimestampAware {

    @jakarta.persistence.Id
    @Column(name = "username", nullable = false)
    @NotBlank
    private String userName;

    @jakarta.persistence.Id
    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "value")
    private byte[] value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSetting setting)) return false;
        return Objects.equals(userName, setting.userName) && Objects.equals(name, setting.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, name);
    }

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
