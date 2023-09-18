package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.lang.ObjectUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.thymeleaf.context.IContext;

import java.util.Collection;

import static net.microfalx.bootstrap.web.template.TemplateUtils.getModelAttribute;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.FormatterUtils.formatNumber;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Template utilities around data sets
 */
public class DataSetTool<M, F extends Field<M>, ID> extends AbstractTool {

    public static final String BOOLEAN_CHECKED = "<i class=\"far fa-check-square\"></i>";
    public static final String BOOLEAN_UNCHECKED = "<i class=\"far fa-square\"></i>";

    private DataSetService dataSetService;

    public DataSetTool(IContext context, DataSetService dataSetService) {
        super(context);
        requireNotEmpty(dataSetService);
        this.dataSetService = dataSetService;
    }

    /**
     * Returns the current data set.
     *
     * @return a non-null instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DataSet<M, F, ID> getDataSet() {
        DataSet dataset = getModelAttribute(context, "dataset");
        if (dataset == null) throw new DataSetException("A data set is not available in the context");
        return dataset;
    }

    /**
     * Returns the data set sort.
     *
     * @return a non-null instance
     */
    public Sort getSort() {
        Sort sort = getModelAttribute(context, "sort");
        if (sort == null) sort = Sort.unsorted();
        return sort;
    }

    /**
     * Returns a list of fields displayed to edit an existing record.
     *
     * @return a non-null instance
     */
    public Collection<Field<M>> getFields() {
        DataSet<M, F, ID> dataSet = getDataSet();
        return dataSet.getVisibleFields();
    }

    /**
     * Returns whether a field is visible for the current state of the data set.
     *
     * @param field the field
     * @return {@code true} if visible, {@code false} otherwise
     */
    public boolean isVisible(Field<M> field) {
        DataSet<M, F, ID> dataSet = getDataSet();
        return dataSet.isVisible(field);
    }

    /**
     * Returns the index of the current row.
     *
     * @return the index
     */
    public long getCurrentIndex() {
        MutableLong index = getModelAttribute(context, "index");
        return index.getAndIncrement();
    }

    /**
     * Returns the records for the current data set.
     *
     * @return a non-null instance
     */
    public Iterable<M> getModels() {
        return getPage().getContent();
    }

    /**
     * Returns the current page with models for the data set.
     *
     * @return a non-null instance
     */
    public Page<M> getPage() {
        Page<M> page = getModelAttribute(context, "page");
        if (page == null) page = Page.empty();
        return page;
    }

    /**
     * Returns whether the data set supports adding a new model.
     *
     * @return {@code true} if add can be enabled, {@code false} otherwise
     */
    public boolean canAdd() {
        DataSet<M, F, ID> dataSet = getDataSet();
        return !dataSet.isReadOnly();
    }

    /**
     * Returns whether the data set is in view mode.
     *
     * @return {@code true} in view mode, {@code false} otherwise
     */
    public boolean isView() {
        return getDataSet().getState() == State.VIEW;
    }

    /**
     * Returns whether the data set has at least one actions.
     *
     * @return {@code true} if a list one action, {@code false} otherwise
     */
    public boolean hasActions() {
        Menu menu = getModelAttribute(context, "actions");
        return menu != null ? menu.hasChildren() : false;
    }

    /**
     * Returns whether the data has a row containing details about the model.
     *
     * @return {@code true} if has details, {@code false} otherwise
     */
    public boolean hasDetails() {
        return getModelAttribute(context, "detailTemplate") != null;
    }

    /**
     * Returns whether the data set has more pages after the current one.
     *
     * @return {@code true} if there are more pages, {@code false} if this is the last one
     */
    public boolean hasNext() {
        return getPage().hasNext();
    }

    /**
     * Returns information about current page.
     *
     * @return a non-null instance
     */
    public String getPageInfo() {
        return getPageInfo(getPage());
    }

    /**
     * Returns information about current page and records.
     *
     * @return a non-null instance
     */
    public String getPageAndRecordInfo() {
        return getPageAndRecordInfo(getPage());
    }

    /**
     * Returns the next page number.
     *
     * @return a positive integer
     */
    public int getNextPage() {
        return getPage().getNumber() + 1;
    }

    /**
     * Returns the class to be used with a header cell.
     *
     * @param field the field
     * @return the input type
     */
    public boolean isReadOnly(Field<M> field) {
        DataSet<M, F, ID> dataSet = getDataSet();
        if (dataSet.isReadOnly() || dataSet.getState() == State.VIEW) return true;
        return field.isReadOnly();
    }

    /**
     * Returns the class for the row used to show the field of a model.
     *
     * @param model the current model
     * @return the class
     */
    public String getPrimaryRowClass(M model) {
        String css = null;
        if (hasDetails()) css = "borderless";
        return css;
    }

    /**
     * Returns the class for the row used to show the details of a model.
     *
     * @param model the current model
     * @return the class
     */
    public String getDetailsRowClass(M model) {
        return "detail";
    }

    /**
     * Returns the class to be used with a header cell.
     *
     * @param field the field
     * @return the input type
     */
    public String getInputType(Field<M> field) {
        if (field.getDataType().isNumeric()) {
            return "numeric";
        } else if (field.getDataType().isBoolean()) {
            return "checkbox";
        } else if (field.getDataType() == Field.DataType.DATE) {
            return "date";
        } else if (field.getDataType() == Field.DataType.DATE_TIME) {
            return "datetime-local";
        } else if (field.getDataType() == Field.DataType.TIME) {
            return "time";
        } else {
            Component.Type componentType = getComponentType(field);
            return componentType == Component.Type.TEXT_FIELD ? "text" : "password";
        }
    }

    /**
     * Returns whether the field associated with the field should be an INPUT type.
     *
     * @param field the field
     * @return {@code true} if of type INPUT, {@code false} otherwise
     */
    public boolean isInputField(Field<M> field) {
        return isVisible(field) && getComponentType(field) == Component.Type.TEXT_FIELD;
    }

    /**
     * Returns whether the field associated with the field should be a TEXT_AREA type.
     *
     * @param field the field
     * @return {@code true} if of type INPUT, {@code false} otherwise
     */
    public boolean isTextAreaField(Field<M> field) {
        return isVisible(field) && getComponentType(field) == Component.Type.TEXT_AREA;
    }

    /**
     * Returns the number columns for a text area field.
     *
     * @param field the field
     * @return the number of columns
     */
    public int getInputColumns(Field<M> field) {
        Component componentAnnot = field.findAnnotation(Component.class);
        return componentAnnot != null ? componentAnnot.columns() : -1;
    }

    /**
     * Returns the number rows for a text area field.
     *
     * @param field the field
     * @return the number of columns
     */
    public int getInputRows(Field<M> field) {
        Component componentAnnot = field.findAnnotation(Component.class);
        int rows = componentAnnot != null ? componentAnnot.rows() : -1;
        if (rows <= 0) rows = 3;
        return rows;
    }

    /**
     * Returns the CSS class(es) for a given field label.
     *
     * @param field the field
     * @return the classes
     */
    public String getInputLabelClass(Field<M> field) {
        String classes = "col-sm-3 col-form-label-sm";
        if (field.getDataType().isBoolean()) {
            classes += " form-check-label";
        }
        return classes.trim();
    }

    /**
     * Returns the CSS class(es) for a given field container.
     *
     * @param field the field
     * @return the classes
     */
    public String getInputContainerClass(Field<M> field) {
        if (isVisible(field)) {
            String classes = "col-sm-9";
            if (field.getDataType().isBoolean()) {
                classes += " form-check";
            }
            return classes.trim();
        } else {
            return "d-none";
        }
    }

    /**
     * Returns the CSS class(es) for a given field.
     *
     * @param field the field
     * @return the classes
     */
    public String getInputFieldClass(Field<M> field) {
        String classes = "";
        if (field.getDataType().isBoolean()) {
            classes += " form-check-input";
        } else {
            classes += " form-control-sm form-control";
        }
        return classes.trim();
    }

    /**
     * Returns the class to be used with a header cell.
     *
     * @param field the field
     * @return the class
     */
    public String getHeaderClass(Field<M> field) {
        String classes = "sortable";
        Sort sort = getSort();
        Sort.Order order = sort.getOrderFor(field.getName());
        if (order != null) {
            if (order.getDirection().isAscending()) {
                classes += " asc";
            } else {
                classes += " desc";
            }
        }
        String cellClass = getCellClass(field);
        if (cellClass != null) {
            classes += " " + cellClass;
        }
        classes = classes.trim();
        return isNotEmpty(classes) ? classes : null;
    }

    /**
     * Returns the class to be used with a row cell.
     *
     * @param field the field
     * @return the class
     */
    public String getCellClass(Field<M> field) {
        String classes = "";
        if (field.getDataType() == Field.DataType.BOOLEAN) {
            classes += " text-center";
        } else if (field.getDataType().isNumeric()) {
            classes += " text-right";
        }
        if (field.getDataType().isTemporal()) classes += " text-nowrap";
        classes = classes.trim();
        return isNotEmpty(classes) ? classes : null;
    }

    /**
     * Returns the display value for a field of the model.
     *
     * @param model the model
     * @param field the field
     * @return the display value
     */
    public String getDisplayValue(M model, Field<M> field) {
        DataSet<M, F, ID> dataSet = getDataSet();
        if (field.getDataType() == Field.DataType.BOOLEAN) {
            boolean bool = Boolean.TRUE.equals(field.get(model));
            return bool ? BOOLEAN_CHECKED : BOOLEAN_UNCHECKED;
        } else {
            return dataSet.getDisplayValue(model, field);
        }
    }

    /**
     * Returns the display value for a field of the model.
     *
     * @param model     the model
     * @param fieldName the field name
     * @return the display value
     */
    public String getDisplayValue(M model, String fieldName) {
        if (model == null) return EMPTY_STRING;
        requireNotEmpty(fieldName);
        DataSet<M, F, ID> dataSet = getDataSet();
        Field<M> field = dataSet.getMetadata().get(fieldName);
        return dataSet.getDisplayValue(model, field);
    }

    /**
     * Returns the value for a field of the model.
     *
     * @param model the model
     * @param field the field
     * @return the display value
     */
    public String getValue(M model, Field<M> field) {
        if (model == null) return null;
        DataSet<M, F, ID> dataSet = getDataSet();
        if (dataSet.getState() == State.VIEW || dataSet.getState() == State.BROWSE) {
            return getDisplayValue(model, field);
        } else {
            return ObjectUtils.toString(field.get(model));
        }
    }

    /**
     * Returns the components identifier of the record in its string form
     *
     * @param model the model
     * @return a non-null instance
     */
    public String getId(M model) {
        DataSet<M, F, ID> dataSet = getDataSet();
        return dataSet.getCompositeId(model).toString();
    }

    /**
     * Returns information about current page.
     *
     * @return a non-null instance
     */
    public static <M> String getPageInfo(Page<M> page) {
        requireNonNull(page);
        return formatNumber(page.getTotalPages()) + " page(s) (" + formatNumber(page.getTotalElements()) + ")";
    }

    /**
     * Returns information about current page and records.
     *
     * @return a non-null instance
     */
    public static <M> String getPageAndRecordInfo(Page<M> page) {
        requireNonNull(page);
        return "Page " + (formatNumber(page.getNumber() + 1)) + " of " + formatNumber(page.getTotalPages())
                + " (" + formatNumber(page.getTotalElements()) + ")";
    }

    private Component.Type getComponentType(Field<M> field) {
        Component componentAnnot = field.findAnnotation(Component.class);
        return componentAnnot != null ? componentAnnot.value() : Component.Type.TEXT_FIELD;
    }
}
