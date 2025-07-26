package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.ClassUtils;

/**
 * Callback interface for data set export operations.
 */
public interface DataSetExportCallback<M, F extends Field<M>, ID> {

    /**
     * Returns whether this callback supports the given data set.
     *
     * @param dataSet the data set
     * @return {@code true} if the callback supports the data set, {@code false} otherwise
     */
    default boolean supports(DataSet<M, F, ID> dataSet) {
        Class<Object> type = ClassUtils.getClassParametrizedType(getClass(), 0);
        return ClassUtils.isSubClassOf(dataSet.getMetadata().getModel(), type);
    }

    /**
     * Returns the name of the field to be exported in the context of a data set.
     *
     * @param dataSet the data set being exported
     * @param field   the field for which the name is being retrieved
     * @return the name of the field, which is typically the field's name
     */
    default String getFieldName(DataSet<M, F, ID> dataSet, F field) {
        return field.getName();
    }

    /**
     * Returns the label of the field to be exported in the context of a data set.
     *
     * @param dataSet the data set being exported
     * @param field   the field for which the name is being retrieved
     * @return the name of the field, which is typically the field's name
     */
    default String getLabel(DataSet<M, F, ID> dataSet, F field) {
        return field.getLabel();
    }

    /**
     * Returns whether a field is exportable in the context of a data set.
     *
     * @param dataSet the data set being exported
     * @param field   the field to check for exportability
     * @return {@code true} if the field is exportable, {@code false} otherwise
     */
    default boolean isExportable(DataSet<M, F, ID> dataSet, F field, boolean exportable) {
        return exportable;
    }

    /**
     * Returns the value to be exported for a given field in a model.
     *
     * @param dataSet the data set being exported
     * @param field   the field for which the value is being retrieved
     * @param model   the model instance from which the value is retrieved
     * @param value   the value to be exported, which can be null or an object of any type
     * @return the value exported
     */
    default Object getValue(DataSet<M, F, ID> dataSet, F field, M model, Object value) {
        return value;
    }
}
