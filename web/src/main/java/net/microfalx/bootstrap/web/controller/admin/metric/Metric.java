package net.microfalx.bootstrap.web.controller.admin.metric;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.model.IdentityAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.ReadOnly;
import net.microfalx.metrics.Meter;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@ReadOnly
public abstract class Metric extends IdentityAware<String> {

    @Position(2)
    @Description("The group of the timer")
    private String group;

    @Position(3)
    @Name
    @Description("The name of the timer")
    private String name;

    @Position(101)
    @Description("The timestamp when the metric was accessed first time")
    private LocalDateTime firstAccess;

    @Position(102)
    @Description("The timestamp when the metric was accessed last time")
    private LocalDateTime lastAccess;

    static void update(Metric model, Meter meter) {
        model.setId(meter.getId());
        model.setName(meter.getName());
        model.setGroup(meter.getGroup());
        model.setFirstAccess(meter.getFirstAccess());
        model.setLastAccess(meter.getLastAccess());
    }
}

