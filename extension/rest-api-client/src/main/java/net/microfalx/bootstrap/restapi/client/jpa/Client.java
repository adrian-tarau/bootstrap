package net.microfalx.bootstrap.restapi.client.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.NaturalId;
import net.microfalx.lang.annotation.Visible;

@Entity(name = "RestApiClient")
@Table(name = "rest_api_clients")
@Getter
@Setter
@ToString(callSuper = true)
public class Client extends NamedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id", nullable = false)
    @NotBlank
    @NaturalId
    @Visible(false)
    private String naturalId;

    @Column(name = "uri")
    private String uri;

    @Column(name = "api_key")
    private String apiKey;
}
