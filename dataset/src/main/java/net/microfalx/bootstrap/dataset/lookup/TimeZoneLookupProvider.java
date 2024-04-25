package net.microfalx.bootstrap.dataset.lookup;

import net.microfalx.bootstrap.dataset.AbstractLookupProvider;
import net.microfalx.bootstrap.dataset.StringLookup;
import net.microfalx.lang.annotation.Provider;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;

@Provider
public class TimeZoneLookupProvider extends AbstractLookupProvider<TimeZoneLookupProvider.TimeZoneLookup, String> {

    @Override
    public Iterable<TimeZoneLookup> doFindAll() {
        Collection<TimeZoneLookup> values = new ArrayList<>();
        for (String zoneId : ZoneId.getAvailableZoneIds()) {
            values.add(new TimeZoneLookup(zoneId, zoneId));
        }
        return values;
    }

    static class TimeZoneLookup extends StringLookup {

        public TimeZoneLookup(String id, String name) {
            super(id, name);
        }
    }
}
