package net.microfalx.bootstrap.jdbc.entity.surrogate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Width;

import java.io.Serializable;

/**
 * A base class for all entities which can be named, tagged and have an optional description.
 */
@MappedSuperclass
@ToString(callSuper = true)
@Getter
@Setter
public abstract class NamedAndTaggedIdentityAware<T extends Serializable> extends NamedIdentityAware<T> {

    @Column(name = "tags")
    @Position(400)
    @Component(Component.Type.TEXT_AREA)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;
}
