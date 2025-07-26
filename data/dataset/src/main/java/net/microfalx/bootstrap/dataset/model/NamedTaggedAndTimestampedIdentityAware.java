package net.microfalx.bootstrap.dataset.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Width;

/**
 * A base class for all models which can be named and tagged and have timestamps.
 */
@Getter
@Setter
@ToString(callSuper = true)
public abstract class NamedTaggedAndTimestampedIdentityAware extends NamedAndTimestampedIdentityAware {

    @Position(400)
    @Component(Component.Type.TEXT_AREA)
    @Description("A collection of tags associated with a {name}")
    @Width("150px")
    @Filterable()
    private String tags;
}
