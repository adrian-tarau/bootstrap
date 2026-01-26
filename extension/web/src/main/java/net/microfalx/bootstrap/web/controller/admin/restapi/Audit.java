package net.microfalx.bootstrap.web.controller.admin.restapi;

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
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity(name = "RestApiClientAdminAudit")
@Table(name = "rest_api_client_audits")
@Getter
@Setter
@ReadOnly
@ToString(callSuper = true)
public class Audit extends IdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    @NotBlank
    @Position(1)
    @Description("The client used for the request")
    @Width("120px")
    private Client client;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Name
    @Position(5)
    @Description("A name for the request")
    @Width("160px")
    private String name;

    @Column(name = "success", nullable = false)
    @Position(10)
    @Label(group = "Response", value = "Success")
    @Description("Indicates whether the request was successful")
    @Width("80px")
    private boolean success;

    @Column(name = "response_status", nullable = false)
    @Position(11)
    @Label(group = "Response", value = "Status")
    @Formattable(alert = HttpStatusProvider.class)
    @Description("The HTTP status code returned by the server")
    @Width("80px")
    private int responseStatus;

    @Column(name = "response_length", nullable = false)
    @Position(12)
    @Label(group = "Response", value = "Length")
    @Formattable(unit = Formattable.Unit.BYTES, negativeValue = Formattable.NA)
    @Description("The response length returned by the server")
    @Width("80px")
    private int responseLength;

    @Column(name = "request_method", nullable = false)
    @NotBlank
    @Label(group = "Request", value = "Method")
    @Position(20)
    @Description("The HTTP method used for the request")
    @Width("80px")
    private String requestMethod;

    @Column(name = "request_path", nullable = false)
    @NotBlank
    @Position(21)
    @Label(group = "Request", value = "Path")
    @Description("The request path used for the request")
    @Width("150px")
    private String requestPath;

    @Column(name = "request_query", nullable = false)
    @Position(22)
    @Label(group = "Request", value = "Query")
    @Description("The query parameters for the request")
    @Width("150px")
    private String requestQuery;

    @Column(name = "started_at", nullable = false, updatable = false)
    @NotNull
    @Position(500)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The timestamp when the request was started")
    @Formattable(tooltip = EntityFormatters.CreatedAtTooltip.class)
    @net.microfalx.bootstrap.dataset.annotation.OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    @NotNull
    @Position(501)
    @Visible(value = false)
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
