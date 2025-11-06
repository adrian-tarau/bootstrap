package net.microfalx.bootstrap.jdbc.support;

/**
 * An interface which represents a database view.
 *
 * @param <V> the view type
 */
public interface View<V extends View<V>> extends Table<V> {

}
