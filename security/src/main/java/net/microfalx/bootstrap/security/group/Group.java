package net.microfalx.bootstrap.security.group;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.Position;

@Entity
@Table(name = "security_groups")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
public class Group extends NamedTimestampAware {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "enabled", nullable = false)
    @Position(10)
    private boolean enabled;
}
