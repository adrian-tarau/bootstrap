package net.microfalx.bootstrap.jdbc.support;

/**
 * Base class for database views.
 *
 * @param <V> the view type
 */
public abstract class AbstractView<V extends AbstractView<V>> extends AbstractTable<V> implements View<V> {

    public AbstractView(Schema schema, String name) {
        super(schema, name);
    }

    @Override
    protected Columns loadColumns() {
        return null;
    }

    @Override
    protected Indexes loadIndexes() {
        return null;
    }
}
