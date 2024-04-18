package net.microfalx.bootstrap.web.controller.support.store;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.store.StoreUtils;
import net.microfalx.lang.annotation.*;
import net.microfalx.metrics.Timer;

import java.time.Duration;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Name("Stores")
@ReadOnly
public class Store {

    @Id
    @Visible(value = false)
    private String id;

    @Position(1)
    @Name
    @Description("The name of the store")
    private String name;

    @Position(20)
    @Label(value = "Count", group = "Memory")
    @Description("The estimated number of objects in memory")
    private long memoryCount;

    @Position(21)
    @Label(value = "Size", group = "Memory")
    @Description("The estimated size of objects in the memory")
    @Formattable(unit = Formattable.Unit.BYTES)
    private long memorySize;

    @Position(22)
    @Label(value = "Count", group = "Disk")
    @Description("The estimated number of objects on disk")
    private long diskCount;

    @Position(23)
    @Label(value = "Size", group = "Disk")
    @Description("The estimated size of objects on disk")
    @Formattable(unit = Formattable.Unit.BYTES)
    private long diskSize;

    @Position(30)
    @Label(value = "Add", group = "Operations")
    @Description("The number of additions to the store")
    private long addCount;

    @Position(31)
    @Label(value = "Remove", group = "Operations")
    @Description("The number of removes from the store")
    private long removeCount;

    @Position(32)
    @Label(value = "Find", group = "Operations")
    @Description("The number of finds from the store")
    private long findCount;

    @Position(33)
    @Label(value = "Walk", group = "Operations")
    @Description("The number of walks from the store")
    private long walkCount;

    @Position(34)
    @Label(value = "Flush", group = "Operations")
    @Description("The number of flushes from the store")
    private long flushCount;

    @Position(40)
    @Label(value = "Add", group = "Statistics")
    @Description("The average duration of an add")
    private Duration addDuration;

    @Position(41)
    @Label(value = "Remove", group = "Statistics")
    @Description("The average duration of a remove")
    private Duration removeDuration;

    @Position(42)
    @Label(value = "Find", group = "Statistics")
    @Description("The average duration of a find")
    private Duration findDuration;

    @Position(43)
    @Label(value = "Walk", group = "Statistics")
    @Description("The average duration of a walk")
    private Duration walkDuration;

    @Position(44)
    @Label(value = "Flush", group = "Statistics")
    @Description("The average duration of a flush")
    private Duration flushDuration;

    @Position(100)
    private String location;

    public static Store from(net.microfalx.bootstrap.store.Store<?, ?> store) {
        Store model = new Store();
        model.setId(store.getOptions().getId());
        model.setName(store.getOptions().getName());
        model.setLocation(store.getDirectory().toURI().getPath());
        model.setMemoryCount(store.count(net.microfalx.bootstrap.store.Store.Location.MEMORY));
        model.setMemorySize(store.size(net.microfalx.bootstrap.store.Store.Location.MEMORY));
        model.setDiskCount(store.count(net.microfalx.bootstrap.store.Store.Location.DISK));
        model.setDiskSize(store.size(net.microfalx.bootstrap.store.Store.Location.DISK));
        Timer timer = StoreUtils.getTimer(StoreUtils.ADD_ACTION, store);
        model.setAddCount(timer.getCount());
        model.setAddDuration(timer.getAverageDuration());
        timer = StoreUtils.getTimer(StoreUtils.REMOVE_ACTION, store);
        model.setRemoveCount(timer.getCount());
        model.setRemoveDuration(timer.getAverageDuration());
        timer = StoreUtils.getTimer(StoreUtils.FIND_ACTION, store);
        model.setFindCount(timer.getCount());
        model.setFindDuration(timer.getAverageDuration());
        timer = StoreUtils.getTimer(StoreUtils.WALK_ACTION, store);
        model.setWalkCount(timer.getCount());
        model.setWalkDuration(timer.getAverageDuration());
        timer = StoreUtils.getTimer(StoreUtils.FLUSH_ACTION, store);
        model.setFlushCount(timer.getCount());
        model.setFlushDuration(timer.getAverageDuration());
        return model;
    }
}
