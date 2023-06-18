package net.microfalx.bootstrap.web.component.form;

/**
 * A field which displays a date, time or date/time, single or as a range.
 */
public class DateTime extends BaseField<DateTime> {

    private boolean time;
    private boolean range;

    public boolean isTime() {
        return time;
    }

    public DateTime setTime(boolean time) {
        this.time = time;
        return this;
    }

    public boolean isRange() {
        return range;
    }

    public DateTime setRange(boolean range) {
        this.range = range;
        return this;
    }
}
