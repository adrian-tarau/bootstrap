package net.microfalx.bootstrap.jdbc.entity.natural;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Width;

import java.io.Serializable;

/**
 * A base class for all entities which can be named and tagged and have timestamps.
 */
@MappedSuperclass
@ToString(callSuper = true)
@Getter
@Setter
public abstract class NamedAndTaggedAndTimestampedIdentityAware<T extends Serializable> extends NamedAndTimestampedIdentityAware<T> {

    @Column(name = "tags")
    @Position(400)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;
}
