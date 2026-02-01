package net.microfalx.bootstrap.dos.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.NaturalId;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

@Entity(name = "DosRule")
@Table(name = "dos_rules")
@Getter
@Setter
@ToString
public class Rule extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id")
    @NotBlank
    @NaturalId
    @Visible(false)
    private String naturalId;

    @Column(name = "address")
    @Position(10)
    @Description("The IP address or CIDR range the rule applies to")
    private String address;

    @Column(name = "hostname")
    @Position(11)
    @Description("The hostname the rule applies to")
    private String hostname;

    @Column(name = "active")
    @Position(20)
    @Description("Indicates whether the rule is active")
    private boolean active;

    @Column(name = "type")
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Position(21)
    @Description("The type of rule, IP or CIDR")
    private net.microfalx.bootstrap.dos.Rule.Type type;

    @Column(name = "action")
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Position(22)
    @Description("The action to be taken when the rule is matched")
    private net.microfalx.bootstrap.dos.Rule.Action action;

    @Column(name = "request_rate")
    @Position(30)
    @Description("The request rate limit associated with the rule (unit of measures: r/s, r/m, r/h)")
    private String requestRate;

    public net.microfalx.bootstrap.dos.Rule toRule() {
        return net.microfalx.bootstrap.dos.Rule.create(address, type).active(active)
                .name(getName()).description(getDescription()).setRequestRate(requestRate)
                .action(action).hostName(hostname)
                .build();
    }

}
