package net.microfalx.bootstrap.security.audit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.security.user.User;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.ReadOnly;
import net.microfalx.lang.annotation.Visible;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit")
@ReadOnly
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Audit {

    public static String OPEN = "Open";
    public static String ADD = "Add";
    public static String EDIT = "Edit";
    public static String DELETE = "Delete";
    public static String EXECUTE = "Execute";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @Visible(false)
    private Integer id;

    @JoinColumn(name = "username", nullable = false)
    @NotNull
    @ManyToOne
    @Position(10)
    private User user;

    @Column(name = "action", nullable = false)
    @NotBlank
    @Position(2)
    private String action;

    @Column(name = "module", nullable = false)
    @NotBlank
    @Position(3)
    private String module;

    @Column(name = "category", nullable = false)
    @NotBlank
    @Position(4)
    private String category;

    @Column(name = "client_info", nullable = false)
    @NotBlank
    @Position(11)
    private String clientInfo;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @Position(1)
    @OrderBy(OrderBy.Direction.DESC)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reference", nullable = false)
    @NotBlank
    @Position(30)
    @Name
    private String reference;

    @Column(name = "error_code", nullable = false)
    @NotBlank
    @Position(31)
    private String errorCode;

    @Column(name = "description")
    @Position(1000)
    @Component(Component.Type.TEXT_AREA)
    private String description;
}
