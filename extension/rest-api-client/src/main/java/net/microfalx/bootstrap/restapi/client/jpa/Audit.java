package net.microfalx.bootstrap.restapi.client.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.surrogate.IdentityAware;

import java.time.LocalDateTime;

@Entity(name = "RestApiClientAudit")
@Table(name = "rest_api_client_audits")
@Getter
@Setter
@ToString(callSuper = true)
public class Audit extends IdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "request_method", nullable = false)
    @NotBlank
    private String requestMethod;

    @Column(name = "response_status", nullable = false)
    private int responseStatus;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "request_path", nullable = false)
    @NotBlank
    private String requestPath;

    @Column(name = "request_query", nullable = false)
    private String requestQuery;

    @Column(name = "response_length", nullable = false)
    private int responseLength;

    @Column(name = "started_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    @NotNull
    private LocalDateTime endedAt;

    @Column(name = "duration")
    private int duration;

    @Column(name = "error_message")
    private String errorMessage;

}
