package net.microfalx.bootstrap.restapi.client.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.EntityFormatters;
import net.microfalx.bootstrap.jdbc.entity.surrogate.IdentityAware;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.annotation.*;

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
    @Name
    @Position(5)
    @Description("A name for a {name}")
    @Width("200px")
    private String name;

    @Column(name = "request_method", nullable = false)
    @NotBlank
    @Position(10)
    @Description("The HTTP method used for the request")
    @Width("80px")
    private String requestMethod;

    @Column(name = "response_status", nullable = false)
    @Position(11)
    @Formattable(alert = HttpStatusProvider.class)
    @Description("The HTTP status code returned by the server")
    @Width("80px")
    private int responseStatus;

    @Column(name = "success", nullable = false)
    @Position(12)
    @Description("Indicates whether the request was successful")
    @Width("80px")
    private boolean success;

    @Column(name = "request_path", nullable = false)
    @NotBlank
    @Position(20)
    @Description("The request path used for the request")
    @Width("150px")
    private String requestPath;

    @Column(name = "request_query", nullable = false)
    @Position(10)
    @Description("The query parameters for the request")
    @Width("150px")
    private String requestQuery;

    @Column(name = "response_length", nullable = false)
    @Position(11)
    @Formattable(unit = Formattable.Unit.BYTES)
    @Description("The response length returned by the server")
    @Width("80px")
    private int responseLength;

    @Column(name = "started_at", nullable = false, updatable = false)
    @NotNull
    @Position(500)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The timestamp when the request was started")
    @Formattable(tooltip = EntityFormatters.CreatedAtTooltip.class)
    @net.microfalx.bootstrap.dataset.annotation.OrderBy(OrderBy.Direction.DESC)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    @NotNull
    @Position(501)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The timestamp when the request has ended")
    @Formattable(tooltip = EntityFormatters.ModifiedAtTooltip.class)
    private LocalDateTime endedAt;

    @Column(name = "duration")
    @Position(502)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The duration of the request")
    @Formattable(unit = Formattable.Unit.MICRO_SECOND)
    private int duration;

    @Column(name = "error_message")
    @Visible(false)
    private String errorMessage;

    public static class HttpStatusProvider implements Formattable.AlertProvider<Audit, Field<Audit>, Integer> {

        @Override
        public Alert provide(Integer value, Field<Audit> field, Audit model) {
            Alert.Type type;
            if (value >= 500) {
                type = Alert.Type.DANGER;
            } else if (value >= 400) {
                type = Alert.Type.WARNING;
            } else {
                type = Alert.Type.SUCCESS;
            }
            return Alert.builder().type(type).message(model.getErrorMessage()).build();
        }
    }
}
