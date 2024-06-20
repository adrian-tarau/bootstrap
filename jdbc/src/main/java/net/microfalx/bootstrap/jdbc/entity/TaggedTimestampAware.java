package net.microfalx.bootstrap.jdbc.entity;

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

/**
 * A base class for all entities which can be tagged and have timestamps.
 */
@MappedSuperclass
@ToString
@Getter
@Setter
public class TaggedTimestampAware extends TimestampAware {

    @Column(name = "tags")
    @Position(400)
    @Component(Component.Type.TEXT_AREA)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;
}
