package net.microfalx.bootstrap.web.component.grid;

import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.panel.BasePanel;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A component specialized in displaying a scrollable table.
 */
public class Grid<M, ID> extends BasePanel<Grid<M, ID>> {

    private final List<Column> columns = new ArrayList<>();
    private DataSet<M, ? extends Field<M>, ID> dataSet;

    public Grid(DataSet<M, ? extends Field<M>, ID> dataSet) {
        requireNonNull(dataSet);
        this.dataSet = dataSet;
        this.setTitle(dataSet.getMetadata().getName());
    }

    /**
     * Returns the data set attached to the grid.
     *
     * @return a non-null instance
     */
    public DataSet<M, ? extends Field<M>, ID> getDataSet() {
        return dataSet;
    }

    /**
     * Returns the registered columns.
     *
     * @return a non-null instance
     */
    public List<Column> getColumns() {
        return unmodifiableList(columns);
    }

    /**
     * Returns a column by its index.
     *
     * @param index the index
     * @return the column
     */
    public Column getColumn(int index) {
        return columns.get(index);
    }

    /**
     * Registers a new column with the grid.
     *
     * @param column the column
     */
    public Grid addColumn(Column column) {
        requireNonNull(column);
        column.setIndex(columns.size());
        columns.add(column);
        return this;
    }


}
