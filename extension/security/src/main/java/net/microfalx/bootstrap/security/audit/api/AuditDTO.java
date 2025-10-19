package net.microfalx.bootstrap.security.audit.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.security.user.api.UserDto;
import net.microfalx.bootstrap.security.user.jpa.User;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Timestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Schema(example = "Audit", description = "An audit record")
public class AuditDTO {

    private String action;
    private String module;
    private String category;
    private String clientInfo;
    private String reference;
    private String errorCode;
    private String description;
}
