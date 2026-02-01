package net.microfalx.bootstrap.dos.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.surrogate.IdentityAware;
import net.microfalx.lang.annotation.CreatedAt;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.ReadOnly;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity(name = "DosAudit")
@Table(name = "dos_audit")
@ReadOnly
@Getter
@Setter
@ToString
public class Audit extends IdentityAware<Integer> {

    @JoinColumn(name = "rule_id", nullable = false)
    @NotNull
    @ManyToOne
    @Position(10)
    @Description("The rule which logged this audit entry")
    private Rule rule;

    @Column(name = "uri")
    @Position(20)
    @Description("The requested URI which triggered the audit entry")
    private String uri;

    @Column(name = "reason")
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Position(21)
    @Description("The reason for which the audit entry was created")
    private net.microfalx.bootstrap.dos.Rule.Reason reason;

    @Column(name = "created_at")
    @Position(30)
    @Description("The timestamp when the audit entry was created")
    @net.microfalx.bootstrap.dataset.annotation.OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime createdAt;

    @Column(name = "description")
    @Description("A description associated with the audit entry")
    private String description;
}
