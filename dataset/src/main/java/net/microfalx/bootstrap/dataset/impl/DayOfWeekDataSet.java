package net.microfalx.bootstrap.dataset.impl;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;

import java.time.DayOfWeek;
import java.util.Arrays;

@Provider
public class DayOfWeekDataSet extends MemoryDataSet<DayOfWeek, PojoField<DayOfWeek>, String> {

    public DayOfWeekDataSet(DataSetFactory<DayOfWeek, PojoField<DayOfWeek>, String> factory, Metadata<DayOfWeek, PojoField<DayOfWeek>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<DayOfWeek> extractModels() {
        return Arrays.asList(DayOfWeek.values());
    }
}
