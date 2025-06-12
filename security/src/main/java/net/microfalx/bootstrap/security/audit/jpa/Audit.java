package net.microfalx.bootstrap.security.audit.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.bootstrap.security.user.jpa.User;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit")
@ReadOnly
@Getter
@Setter
@ToString
public class Audit extends IdentityAware<Integer> {

    public static String OPEN = "Open";
    public static String ADD = "Add";
    public static String EDIT = "Edit";
    public static String DELETE = "Delete";
    public static String EXECUTE = "Execute";

    @JoinColumn(name = "username", nullable = false)
    @NotNull
    @ManyToOne
    @Position(10)
    @Description("The user which performed the action")
    private User user;

    @Column(name = "action", nullable = false)
    @NotBlank
    @Position(2)
    @Description("The action performed by the user")
    private String action;

    @Column(name = "module", nullable = false)
    @NotBlank
    @Position(3)
    @Description("The module where the action was executed")
    private String module;

    @Column(name = "category", nullable = false)
    @NotBlank
    @Position(4)
    @Description("A category for the type of action executed by the user")
    private String category;

    @Column(name = "client_info", nullable = false)
    @NotBlank
    @Position(11)
    @Description("The host/IP/proxy received by the application from the web container")
    private String clientInfo;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @Position(1)
    @OrderBy(OrderBy.Direction.DESC)
    @Timestamp
    @Description("The timestamp when the action was performed")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reference", nullable = false)
    @NotBlank
    @Position(30)
    @Name
    @Description("A reference to the action (the URI or the model identifier)")
    private String reference;

    @Column(name = "error_code", nullable = false)
    @NotBlank
    @Position(31)
    @Description("The HTTP response code")
    private String errorCode;

    @Column(name = "description")
    @Position(1000)
    @Component(Component.Type.TEXT_AREA)
    @Description("A (detailed) description of the action or the results of the action")
    private String description;
}
