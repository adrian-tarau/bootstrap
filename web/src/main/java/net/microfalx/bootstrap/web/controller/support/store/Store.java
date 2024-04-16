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
    @Label(value = "Count", group = "Objects")
    @Description("The estimated number of objects in the store")
    private long count;

    @Position(21)
    @Label(value = "Size", group = "Objects")
    @Description("The estimated size of objects in the store (file system")
    @Formattable(unit = Formattable.Unit.BYTES)
    private long size;

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
    private long listCount;

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
    private Duration listDuration;

    @Position(100)
    private String location;

    public static Store from(net.microfalx.bootstrap.store.Store<?, ?> store) {
        Store model = new Store();
        model.setId(store.getOptions().getId());
        model.setName(store.getOptions().getName());
        model.setLocation(store.getDirectory().toURI().getPath());
        model.setCount(store.count());
        model.setSize(store.size());
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
        model.setListCount(timer.getCount());
        model.setListDuration(timer.getAverageDuration());
        return model;
    }
}
