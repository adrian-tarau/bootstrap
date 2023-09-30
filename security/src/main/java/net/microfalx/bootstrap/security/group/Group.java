package net.microfalx.bootstrap.security.group;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;

@Entity
@Table(name = "security_groups")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Group extends NamedTimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "enabled", nullable = false)
    @Position(10)
    @Description("Indicates whether the {name} is enabled or disabled")
    private boolean enabled;
}
