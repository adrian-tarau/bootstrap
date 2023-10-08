package net.microfalx.bootstrap.web.component.panel;

/**
 * A container which hosts other components, and it organizes as columns within a {@link Row}.
 */
public final class Column extends BasePanel<Column> {

    public static final int AUTO = -1;

    private int span = AUTO;

    public static Column create(int span) {
        return new Column().setSpan(span);
    }

    private Column() {
    }

    /**
     * Returns the number of columns to span.
     *
     * @return the number of columns, {@link #AUTO} for automatic
     */
    public int getSpan() {
        return span;
    }

    /**
     * Changes the number of columns to span.
     *
     * @param span the number of columns, {@link #AUTO} for automatic
     */
    public Column setSpan(int span) {
        this.span = span < 0 ? AUTO : span;
        return this;
    }
}
