package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetException;
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
     * Returns a list of fields displayed in a grid.
     *
     * @return a non-null instance
     */
    public Collection<Field<M>> getBrowsableFields() {
        DataSet<M, F, ID> dataSet = getDataSet();
        return dataSet.getVisibleFields();
    }

    /**
     * Returns a list of fields displayed to edit an existing record.
     *
     * @return a non-null instance
     */
    public Collection<Field<M>> getEditableFields() {
        DataSet<M, F, ID> dataSet = getDataSet();
        dataSet.edit();
        return dataSet.getVisibleFields();
    }

    /**
     * Returns a list of fields displayed to append a new record.
     *
     * @return a non-null instance
     */
    public Collection<Field<M>> getAppendableFields() {
        DataSet<M, F, ID> dataSet = getDataSet();
        dataSet.edit();
        return dataSet.getVisibleFields();
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
}
