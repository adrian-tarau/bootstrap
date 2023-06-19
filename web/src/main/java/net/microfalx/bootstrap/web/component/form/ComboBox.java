package net.microfalx.bootstrap.web.component.form;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSet;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A field which displays a list of items.
 */
public class ComboBox<M, ID> extends BaseField<ComboBox<M, ID>> {

    private DataSet<M, ? extends Field<M>, ID> dataSet;

    public ComboBox(DataSet<M, ? extends Field<M>, ID> dataSet) {
        requireNonNull(dataSet);
        this.dataSet = dataSet;
    }
}
