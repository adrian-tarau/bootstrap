package net.microfalx.bootstrap.web.dataset;

import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.bootstrap.web.chart.provider.ContextAwareChartProvider;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A chart provider which is aware of a data set context.
 */
public abstract class DataSetChartProvider<M, F extends Field<M>, ID> extends ContextAwareChartProvider {

    public static final int HEIGHT = 20;
    public static final int PIE_WIDTH = 20;
    public static final int PIE_HEIGHT = HEIGHT;
    public static final int BAR_WIDTH = 150;
    public static final int BAR_HEIGHT = HEIGHT;

    public static final String ZERO_FILL_COLOR = "#404040";

    public static final String FIELD_ATTR = "field";
    public static final String MODEL_ATTR = "model";
    public static final String DATASET_ATTR = "dataSet";

    @SuppressWarnings("unchecked")
    public final DataSet<M, F, ID> getDataSet(Chart chart) {
        requireNonNull(chart);
        Attribute attribute = chart.getAttributes().get(DATASET_ATTR);
        if (attribute == null) throw new DataSetException("A data set is not associated with chart " + chart);
        return (DataSet<M, F, ID>) attribute.getValue();
    }

    @SuppressWarnings("unchecked")
    public final F getField(Chart chart) {
        requireNonNull(chart);
        Attribute attribute = chart.getAttributes().get(FIELD_ATTR);
        if (attribute == null) throw new DataSetException("A data set field is not associated with chart " + chart);
        return (F) attribute.getValue();
    }

    @SuppressWarnings("unchecked")
    public final M getModel(Chart chart) {
        requireNonNull(chart);
        Attribute attribute = chart.getAttributes().get(MODEL_ATTR);
        if (attribute == null) throw new DataSetException("A data set model is not associated with chart " + chart);
        return (M) attribute.getValue();
    }


}
