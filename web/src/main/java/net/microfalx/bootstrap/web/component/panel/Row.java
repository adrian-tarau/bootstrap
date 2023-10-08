package net.microfalx.bootstrap.web.component.panel;

/**
 * A container which hosts other components, and it organizes them as rows.
 */
public final class Row extends BasePanel<Row> {

    public static Row create() {
        return new Row();
    }

    public static Row create(String title) {
        return new Row().setTitle(title);
    }

    private Row() {
    }
}
