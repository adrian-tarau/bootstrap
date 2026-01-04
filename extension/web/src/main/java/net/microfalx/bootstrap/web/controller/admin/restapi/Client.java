package net.microfalx.bootstrap.web.controller.admin.restapi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.formatter.AbstractFormatter;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTimestampedIdentityAware;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.SecretUtils;
import net.microfalx.lang.annotation.*;

@Entity(name = "RestApiClientAdmin")
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
    @Position(20)
    @Description("The base URI of the REST API")
    @Width("300px")
    private String uri;

    @Column(name = "api_key")
    @Position(21)
    @Description("The API Key (or bearer token) used to access the REST API")
    @Width("100")
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.BROWSE})
    @Component(Component.Type.PASSWORD)
    @Formattable(formatter = ApiKeyFormatter.class)
    private String apiKey;

    public static class ApiKeyFormatter extends AbstractFormatter<Client, Field<Client>, String> {

        @Override
        protected String doFormat(String value, Field<Client> field, Client model) {
            return SecretUtils.maskSecret(value);
        }

    }
}
