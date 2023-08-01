package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.thymeleaf.context.IContext;

import java.util.Collection;

import static net.microfalx.bootstrap.web.template.TemplateUtils.getModelAttribute;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Template utilities around data sets
 */
public class DataSetTool<M, F extends Field<M>, ID> extends AbstractTool {

    public static final String BOOLEAN_CHECKED = "<i class=\"far fa-check-square\"></i>";
    public static final String BOOLEAN_UNCHECKED = "<i class=\"far fa-square\"></i>";

    public DataSetTool(IContext context) {
        super(context);
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
     * Returns whether the data set has at least one actions.
     *
     * @return {@code true} if a list one action, {@code false} otherwise
     */
    public boolean hasActions() {
        Menu menu = getModelAttribute(context, "actions");
        return menu != null ? menu.hasChildren() : false;
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
        Page<M> page = getPage();
        return page.getTotalPages() + " page(s) (" + page.getTotalElements() + ")";
    }

    /**
     * Returns information about current page and records.
     *
     * @return a non-null instance
     */
    public String getPageAndRecordInfo() {
        Page<M> page = getPage();
        return "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() + " (" + page.getTotalElements() + ")";
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
     * Returns the CSS class(es) for a given field label.
     *
     * @param field the field
     * @return the classes
     */
    public String getInputLabelClass(Field<M> field) {
        String classes = "col-sm-3 col-form-label";
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
        String classes = "form-control";
        return classes;
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
            classes += "text-right";
        }
        classes = classes.trim();
        return isNotEmpty(classes) ? classes : null;
    }

    /**
     * Returns the display valu for a field of the model.
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
     * Returns the components identifier of the record in its string form
     *
     * @param model the model
     * @return a non-null instance
     */
    public String getId(M model) {
        DataSet<M, F, ID> dataSet = getDataSet();
        return dataSet.getCompositeId(model).toString();
    }

    private Component.Type getComponentType(Field<M> field) {
        Component componentAnnot = field.findAnnotation(Component.class);
        return componentAnnot != null ? componentAnnot.value() : Component.Type.TEXT_FIELD;
    }
}
