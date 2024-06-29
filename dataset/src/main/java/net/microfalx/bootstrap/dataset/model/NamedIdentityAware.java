package net.microfalx.bootstrap.dataset.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.annotation.*;

import java.io.Serial;

/**
 * A base class for all models which can be named and have an optional description.
 */
@Getter
@Setter
@ToString
public abstract class NamedIdentityAware<T> extends IdentityAware<T> implements Nameable {

    @Serial
    private static final long serialVersionUID = -720697127573911840L;

    @Id
    @Position(1)
    @Visible(value = false)
    @NotBlank
    private T id;

    @Name
    @Position(5)
    @Description("A name for a {name}")
    @Width("200px")
    @NotBlank
    private String name;

    @Position(1000)
    @Component(Component.Type.TEXT_AREA)
    @Description("A description for a {name}")
    @Width("300px")
    @Filterable()
    private String description;
}
