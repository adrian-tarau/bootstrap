package net.microfalx.bootstrap.web.template.tools;

import com.google.common.collect.Iterables;
import net.microfalx.bootstrap.dataset.*;
import net.microfalx.bootstrap.dataset.annotation.Align;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Tab;
import net.microfalx.bootstrap.dataset.annotation.Tabs;
import net.microfalx.bootstrap.dataset.formatter.FormatterUtils;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.bootstrap.web.chart.ChartService;
import net.microfalx.bootstrap.web.chart.Options;
import net.microfalx.bootstrap.web.chart.Type;
import net.microfalx.bootstrap.web.chart.animation.Animations;
import net.microfalx.bootstrap.web.chart.annotation.Chartable;
import net.microfalx.bootstrap.web.chart.datalabels.DataLabels;
import net.microfalx.bootstrap.web.chart.style.Stroke;
import net.microfalx.bootstrap.web.chart.tooltip.Tooltip;
import net.microfalx.bootstrap.web.component.Actionable;
import net.microfalx.bootstrap.web.component.Button;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.dataset.DataSetChartProvider;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Width;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import org.apache.commons.lang3.mutable.MutableLong;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.thymeleaf.context.IContext;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.bootstrap.web.template.TemplateUtils.getModelAttribute;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.FormatterUtils.formatNumber;
import static net.microfalx.lang.StringUtils.*;

/**
 * Template utilities around data sets
 */
@SuppressWarnings("unused")
public class DataSetTool<M, F extends Field<M>, ID> extends AbstractTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetTool.class);

    public static final String BOOLEAN_CHECKED = "<i class=\"far fa-check-square\"></i>";
    public static final String BOOLEAN_UNCHECKED = "<i class=\"far fa-square\"></i>";
    private static final String STYLE_SEPARATOR = "; ";

    private final DataSetService dataSetService;
    private final ChartService chartService;

    private final static AtomicBoolean attributeClassesInitialized = new AtomicBoolean();
    private final static Queue<String> attributeClassesQueue = new LinkedBlockingQueue<>();
    private final static Map<String, String> attributeClasses = new ConcurrentHashMap<>();

    private ColumnGroups<M, F, ID> columnGroups;
    private Boolean hasColumnGroups;

    public DataSetTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
        this.dataSetService = applicationContext.getBean(DataSetService.class);
        this.chartService = applicationContext.getBean(ChartService.class);
    }

    /**
     * Returns the current data set.
     *
     * @return a non-null instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DataSet<M, F, ID> getDataSet() {
        return getDataSet(true);
    }

    /**
     * Returns the current data set.
     *
     * @param required {@code true} if the data set is required, {@code false} otherwise
     * @return a non-null instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DataSet<M, F, ID> getDataSet(boolean required) {
        DataSet dataset = getModelAttribute(templateContext, "dataset");
        if (dataset == null && required) throw new DataSetException("A data set is not available in the context");
        return dataset;
    }

    /**
     * Returns the current data set.
     *
     * @return a non-null instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DataSetController<M, ID> getDataSetController() {
        return getDataSetController(true);
    }

    /**
     * Returns the current data set controller.
     *
     * @param required {@code true} if the data set is required, {@code false} otherwise
     * @return a non-null instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DataSetController<M, ID> getDataSetController(boolean required) {
        DataSetController controller = getModelAttribute(templateContext, "controller");
        if (controller == null && required) {
            throw new DataSetException("A data set controller is not available in the context");
        }
        return controller;
    }

    /**
     * Returns the current data set annotation.
     *
     * @param required {@code true} if the annotation is required, {@code false} otherwise
     * @return a non-null instance
     */
    public net.microfalx.bootstrap.dataset.annotation.DataSet getDataSetAnnotation(boolean required) {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnot = getModelAttribute(templateContext, "dataset-annotation");
        if (dataSetAnnot == null && required) {
            throw new DataSetException("A @DataSet annotation is not available in the context");
        }
        return dataSetAnnot;
    }

    /**
     * Returns the fields which are read-only
     *
     * @return a non-null instance
     */
    public Map<String, Boolean> getReadOnlyFields() {
        Map<String, Boolean> readOnlyFields = getModelAttribute(templateContext, "readOnlyFields");
        return ObjectUtils.defaultIfNull(readOnlyFields, Collections.emptyMap());
    }

    /**
     * Returns the data set sort.
     *
     * @return a non-null instance
     */
    public Sort getSort() {
        Sort sort = getModelAttribute(templateContext, "sort");
        if (sort == null) sort = Sort.unsorted();
        return sort;
    }

    /**
     * Returns a list of fields displayed to edit an existing record.
     *
     * @return a non-null instance
     */
    public Collection<F> getFields() {
        DataSet<M, F, ID> dataSet = getDataSet();
        return dataSet.getVisibleFields();
    }

    /**
     * Returns the field index in the grid.
     *
     * @param field the field index
     * @return the index, -1 if it cannot be located
     */
    public int getFieldIndex(Field<M> field) {
        int index = 1;
        Collection<F> fields = getFields();
        for (F visibleField : fields) {
            if (visibleField.equals(field)) return index;
            index++;
        }
        return -1;
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
        MutableLong index = getModelAttribute(templateContext, "index");
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
     * Returns the operator used by the filterable fields.
     *
     * @return a non-null instance  if the operator is available, null otherwise
     */
    public String getFilterableOperator() {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation(false);
        return dataSetAnnotation != null ? dataSetAnnotation.filterOperator() : null;
    }

    /**
     * Returns the quote character used by the filterable fields.
     *
     * @return a non-null instance  if the quote character is available, null otherwise
     */
    public String getFilterableQuoteChar() {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation(false);
        return dataSetAnnotation != null ? String.valueOf(dataSetAnnotation.filterQuoteChar()) : null;
    }

    /**
     * Returns the current page with models for the data set.
     *
     * @return a non-null instance
     */
    public Page<M> getPage() {
        Page<M> page = getModelAttribute(templateContext, "page");
        if (page == null) page = Page.empty();
        return page;
    }

    /**
     * Returns the CSS class for a given attribute in the details row.
     *
     * @param attribute the attribute
     * @return the class
     */
    public String getAttributeClasses(Attribute attribute) {
        requireNonNull(attribute);
        initializeColors();
        String name = attribute.getName();
        String classByValueKey = toDashIdentifier(name) + "_" + toDashIdentifier(attribute.asString());
        String attributeClass = attributeClasses.get(classByValueKey);
        if (attributeClass != null) return attributeClass;
        attributeClass = attributeClasses.get(name.toLowerCase());
        if (attributeClass == null) {
            String availableClass = defaultIfNull(attributeClassesQueue.poll(), EMPTY_STRING);
            attributeClasses.put(name.toLowerCase(), availableClass);
        }
        return defaultIfNull(attributeClass, EMPTY_STRING);
    }

    /**
     * Returns whether the data set has a column groups (fields with a group).
     *
     * @return {@code true} if there are groups, {@code false} otherwise
     */
    public boolean hasColumnGroups() {
        if (hasColumnGroups != null) return hasColumnGroups;
        for (Field<M> field : getFields()) {
            if (isNotEmpty(field.getGroup())) {
                hasColumnGroups = true;
                columnGroups = new ColumnGroups<>(getFields());
                break;
            }
        }
        if (hasColumnGroups == null) hasColumnGroups = false;
        return hasColumnGroups;
    }

    /**
     * Returns the groups of fields used to create the form.
     *
     * @return a non-null instance
     */
    public FormGroups<M, F, ID> getFormGroups() {
        DataSet<M, F, ID> dataSet = getDataSet();
        return new FormGroups<>(dataSet, dataSet.getVisibleFields());
    }

    /**
     * Returns the column groups for the first row.
     *
     * @return a non-null instance
     */
    public Iterable<ColumnGroup<M, F, ID>> getColumnGroupsFirstRow() {
        return columnGroups != null ? columnGroups.getFirstRow() : Collections.emptyList();
    }

    /**
     * Returns the column groups for the first row.
     *
     * @return a non-null instance
     */
    public Iterable<ColumnGroup<M, F, ID>> getColumnGroupsSecondRow() {
        return columnGroups != null ? columnGroups.getSecondRow() : Collections.emptyList();
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
        Menu menu = getModelAttribute(templateContext, "actions");
        return menu != null ? menu.hasChildren() : false;
    }

    /**
     * Returns whether the data has a row containing details about the model.
     *
     * @return {@code true} if has details, {@code false} otherwise
     */
    public boolean hasDetails() {
        return getModelAttribute(templateContext, "detailTemplate") != null;
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
     * Returns whether the field is read-only.
     *
     * @param field the field
     * @return {@code true} if read-only, {@code false} otherwise
     */
    public boolean isReadOnly(Field<M> field) {
        DataSet<M, F, ID> dataSet = getDataSet();
        if (dataSet.isReadOnly() || dataSet.getState() == State.VIEW) return true;
        boolean readOnly = dataSet.isReadOnly(field);
        if (readOnly) return true;
        Boolean customReadOnly = getReadOnlyFields().get(field.getName());
        if (customReadOnly != null) return customReadOnly;
        return false;
    }

    /**
     * Returns whether the field is checked (it only applies to boolean type fields.
     *
     * @param model the model
     * @param field the field
     * @return {@code true} if checked, {@code false} if unchecked, {@code NULL} if the field is not a boolean
     */
    public Boolean isChecked(M model, Field<M> field) {
        DataSet<M, F, ID> dataSet = getDataSet();
        if (field.getDataType().isBoolean()) {
            return Boolean.TRUE.equals(field.get(model));
        } else {
            return null;
        }
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
     * Returns the alert associated with the field for a given model.
     *
     * @param model the model
     * @param field the field
     * @return the alert, null if not present
     */
    public Alert getAlert(M model, Field<M> field) {
        return dataSetService.getAlert(model, field).orElse(null);
    }

    /**
     * Returns the tooltip associated with the field for a given model.
     *
     * @param model the model
     * @param field the field
     * @return the alert, null if not present
     */
    public String getTooltip(M model, Field<M> field) {
        Alert alert = getAlert(model, field);
        if (alert != null && alert.isTooltip()) {
            return alert.getMessage();
        } else {
            return dataSetService.getTooltip(model, field).orElse(null);
        }
    }

    /**
     * Returns the actionable associated with the field for a given model.
     *
     * @param model the model
     * @param field the field
     * @return the alert, null if not present
     */
    public Actionable<?> getActionable(M model, Field<M> field) {
        Object value = field.get(model);
        if (value == null) return null;
        if (hasRenderableMarkup(field)) {
            net.microfalx.bootstrap.dataset.annotation.Renderable renderableAnnot = field.findAnnotation(net.microfalx.bootstrap.dataset.annotation.Renderable.class);
            return new Button().setAction(renderableAnnot.action()).setIcon(renderableAnnot.icon());
        } else {
            return null;
        }
    }

    /**
     * Returns the alert CSS classes.
     *
     * @param alert the alert
     * @return the classes, null if there is no alert
     */
    public String getAlertClass(Alert alert) {
        if (alert == null) return null;
        return "alert alert-" + alert.getType().name().toLowerCase() + " text-center";
    }

    /**
     * Returns the alert icon.
     *
     * @param alert the alert
     * @return the icon classes, null if there is no icon
     */
    public String getAlertIcon(Alert alert) {
        if (alert == null) return null;
        if (isEmpty(alert.getMessage())) {
            return "fa-solid fa-circle fa-blank";
        } else {
            return switch (alert.getIcon()) {
                case BELL -> "fa-solid fa-bell";
                case EXCLAMATION -> "fa-solid fa-triangle-exclamation";
                case INFORMATION -> "fa-solid fa-circle-info";
            };
        }
    }

    /**
     * Returns the alert CSS classes.
     *
     * @param alert the alert
     * @return the classes, null if there is no alert
     */
    public String getAlertStyle(Alert alert) {
        if (alert == null || !alert.hasProperties()) return null;
        StringBuilder builder = new StringBuilder();
        if (alert.getMinWidth() != null) {
            builder.append("min-width: ").append(alert.getMinWidth()).append("px");
        } else if (alert.getWidth() != null) {
            builder.append("width: ").append(alert.getWidth()).append("px");
        }
        String style = builder.toString().trim();
        return isNotEmpty(style) ? style : null;
    }

    /**
     * Returns whether the field has markups (renderers, charts, etc) and the content should be rendered without wrapping.
     *
     * @param field the field
     * @return {@code true} if it has a markups, {@code false} otherwise
     */
    public boolean hasMarkup(Field<M> field) {
        return hasChart(field) || hasFormattableMarkup(field) || hasRenderableMarkup(field);
    }

    /**
     * Returns whether the field has a display value.
     *
     * @param field the field
     * @return {@code true} if it has value, {@code false} otherwise
     */
    public boolean hasDisplayValue(Field<M> field) {
        return !(hasChart(field) || hasRenderableMarkup(field) || isName(field));
    }

    /**
     * Returns whether a field is supposed to be rendered as a chart.
     *
     * @param field the field
     * @return {@code true} if it has a chart, {@code false} otherwise
     */
    public boolean hasChart(Field<M> field) {
        return field.findAnnotation(Chartable.class) != null;
    }

    /**
     * Returns whether a field is supposed to be rendered as a name.
     * @param field the field
     * @return {@code true} if it is a name field, {@code false} otherwise
     */
    public boolean isName(Field<M> field) {
        return  field.isName() && !field.findAnnotation(Name.class).secondary();
    }

    /**
     * Returns whether a field is supposed to be rendered with an alert.
     *
     * @param field the field
     * @return {@code true} if it has a chart, {@code false} otherwise
     */
    public boolean hasFormattableMarkup(Field<M> field) {
        net.microfalx.bootstrap.dataset.annotation.Formattable formattableAnnot = field.findAnnotation(net.microfalx.bootstrap.dataset.annotation.Formattable.class);
        return formattableAnnot != null && formattableAnnot.alert() != net.microfalx.bootstrap.dataset.annotation.Formattable.AlertProvider.class;
    }

    /**
     * Returns whether a field is supposed to be rendered with an action or template.
     *
     * @param field the field
     * @return {@code true} if it has a chart, {@code false} otherwise
     */
    public boolean hasRenderableMarkup(Field<M> field) {
        net.microfalx.bootstrap.dataset.annotation.Renderable renderableAnnot = field.findAnnotation(net.microfalx.bootstrap.dataset.annotation.Renderable.class);
        return renderableAnnot != null && (isNotEmpty(renderableAnnot.action()) || isNotEmpty(renderableAnnot.template()));
    }

    /**
     * Returns whether the field asks to display the summary of the field in addition to the chart.
     *
     * @param field the field
     * @return {@code true} if it has a summary, {@code false} otherwise
     */
    public boolean hasChartSummary(Field<M> field) {
        Chartable chartableAnnot = field.findAnnotation(Chartable.class);
        return chartableAnnot != null && chartableAnnot.displaySummary();
    }

    /**
     * Returns the chart supporting the field.
     * <p>
     * The method will fail if {@link #hasChart(Field)} return false.
     *
     * @param model the model
     * @param field the field
     * @return the chart instance
     */
    public Chart getChart(M model, Field<M> field) {
        Chartable chartableAnnot = field.findAnnotation(Chartable.class);
        if (chartableAnnot == null) throw new DataSetException("Field '" + field.getName() + "' does not have a chart");
        Options options = Options.create(chartableAnnot.type()).sparkline().setAnimations(Animations.disable());
        if (chartableAnnot.width() > 0) options.setWidth(chartableAnnot.width());
        if (chartableAnnot.height() > 0) options.setHeight(chartableAnnot.height());
        Chart chart = Chart.create(options);
        if (chart.getOptions().getType() == Type.BAR) {
            chart.setTooltip(Tooltip.valueWithTimestamp()).setDataLabels(DataLabels.disabled());
        } else {
            chart.setStroke(Stroke.width(1));
            chart.setTooltip(Tooltip.fixed(false));
        }
        Attributes<?> attributes = chart.getAttributes();
        attributes.add(DataSetChartProvider.DATASET_ATTR, getDataSet());
        attributes.add(DataSetChartProvider.FIELD_ATTR, field);
        attributes.add(DataSetChartProvider.MODEL_ATTR, model);
        chart.setProvider(createInstance(chartableAnnot.provider()));
        chartService.register(chart);
        return chart;
    }

    /**
     * Retutrns whether the field should use a text or a password field.
     *
     * @param field the field
     * @return {@code true} if of type is text or password field, {@code false} otherwise
     */
    public boolean isTextField(Field<M> field) {
        Component.Type componentType = getComponentType(field);
        return componentType == Component.Type.TEXT_FIELD || componentType == Component.Type.PASSWORD;
    }

    /**
     * Returns whether the field associated with the field should be an SELECT type (dropdown)
     *
     * @param field the field
     * @return {@code true} if of type INPUT, {@code false} otherwise
     */
    public boolean isDropDownField(Field<M> field) {
        return isVisible(field) && isLookupFieldOrCollection(field);
    }

    /**
     * Returns whether the field associated with the field should be an INPUT type (everything except checkbox)
     *
     * @param field the field
     * @return {@code true} if of type INPUT, {@code false} otherwise
     */
    public boolean isInputField(Field<M> field) {
        return isVisible(field) && !isLookupFieldOrCollection(field) && isTextField(field) && !field.getDataType().isBoolean();
    }

    /**
     * Returns whether the field associated with the field should be an INPUT type of type checkbox.
     *
     * @param field the field
     * @return {@code true} if of type INPUT, {@code false} otherwise
     */
    public boolean isCheckboxField(Field<M> field) {
        return isVisible(field) && isTextField(field) && field.getDataType().isBoolean();
    }

    /**
     * Returns whether the field associated with the field should be a TEXT_AREA type.
     *
     * @param field the field
     * @return {@code true} if of type INPUT, {@code false} otherwise
     */
    public boolean isTextAreaField(Field<M> field) {
        return isVisible(field) && !isLookupFieldOrCollection(field) && getComponentType(field) == Component.Type.TEXT_AREA;
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
            return "col-sm-9";
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
        String classes = field.getDataType().isBoolean() ? EMPTY_STRING : "form-control-sm";
        if (field.getDataType().isBoolean()) {
            classes += " form-check-input";
        } else if (isLookupFieldOrCollection(field)) {
            classes = "form-select form-select-sm";
        } else {
            classes += " form-control";
        }
        return classes.trim();
    }

    /**
     * Returns whether the field is supported by a collection, which means the dropdown has to be with multi-select.
     *
     * @param field the field
     * @return {@code true} if multi-select is required, {@code false} otherwise
     */
    public boolean isMultiSelect(Field<M> field) {
        requireNonNull(field);
        return field.getDataType() == Field.DataType.COLLECTION;
    }

    /**
     * Returns the column span for a field.
     *
     * @param field the field
     * @return the column span, null if there is no column span
     */
    public Integer getHeaderColSpan(Field<M> field) {
        if (hasColumnGroups()) {
            return isEmpty(field.getGroup()) ? 2 : 1;
        } else {
            return null;
        }
    }

    /**
     * Returns the row span for a field.
     *
     * @param field the field
     * @return the row span, null if there is no row span
     */
    public Integer getHeaderRowSpan(Field<M> field) {
        if (hasColumnGroups()) {
            if (field == null) return 2;
            return isEmpty(field.getGroup()) ? 2 : 1;
        } else {
            return null;
        }
    }

    /**
     * Returns the class to be used with a header cell.
     *
     * @param field the field
     * @return the class
     */
    public String getHeaderClass(Field<M> field) {
        return getHeaderClass(field, false);
    }

    /**
     * Returns the class to be used with a header cell.
     *
     * @param field the field
     * @param group {@code true} when the field is part of a column group, {@code false} otherwise
     * @return the class
     */
    public String getHeaderClass(Field<M> field, boolean group) {
        DataSet<M, F, ID> dataSet = getDataSet();
        StringBuilder builder = new StringBuilder("align-middle");
        if (!group) {
            if (!field.isTransient() && dataSet.isSortable(field)) {
                StringUtils.append(builder, "sortable", SPACE);
            }
            Sort sort = getSort();
            Sort.Order order = sort.getOrderFor(field.getName());
            if (order != null) {
                StringUtils.append(builder, (order.getDirection().isAscending() ? "asc" : "desc"), SPACE);
            }
        }
        String cellClass = getCellClass(field, group, true);
        if (cellClass != null) StringUtils.append(builder, cellClass, SPACE);
        StringUtils.append(builder, getHeaderCustomClasses(field), SPACE);
        String classes = builder.toString().trim();
        return isNotEmpty(classes) ? classes : null;
    }

    /**
     * Returns the custom styles to be used with a header cell.
     *
     * @param field the field
     * @return the class
     */
    public String getHeaderStyle(Field<M> field) {
        StringBuilder builder = new StringBuilder();
        Width widthAnnot = field.findAnnotation(Width.class);
        if (widthAnnot != null) {
            if (isNotEmpty(widthAnnot.value())) {
                builder.append("width: ").append(widthAnnot.value());
            } else {
                if (isNotEmpty(widthAnnot.min())) {
                    builder.append("min-width: ").append(widthAnnot.min());
                }
                if (isNotEmpty(widthAnnot.min())) {
                    StringUtils.append(builder, "min-width: " + widthAnnot.min(), STYLE_SEPARATOR);
                }
            }
        }
        String style = builder.toString().trim();
        return style.isEmpty() ? null : style;
    }

    /**
     * Returns the class to be used with a row cell.
     *
     * @param field the field
     * @return the class
     */
    public String getCellClass(Field<M> field) {
        return getCellClass(field, false, false);
    }

    /**
     * Returns the class to be used with a row cell.
     *
     * @param field the field
     * @param group {@code true} when the field is part of a column group, {@code false} otherwise
     * @return the class
     */
    public String getCellClass(Field<M> field, boolean group, boolean header) {
        DataSet<M, F, ID> dataSet = getDataSet();
        String classes = "";
        Align.Type align = getFieldAlign(field, group, header);
        if (align == Align.Type.CENTER) {
            classes += " text-center";
        } else if (align == Align.Type.RIGHT) {
            classes += " text-end";
        }
        if (field.getDataType().isTemporal() || hasMarkup(field)) classes += " text-nowrap";
        if (dataSet.isFilterable(field) && !hasMarkup(field)) classes += " filterable";
        classes = classes.trim();
        return isNotEmpty(classes) ? classes : null;
    }

    public Align.Type getFieldAlign(Field<M> field, boolean group, boolean header) {
        Align.Type type = Align.Type.AUTO;
        if (field.getDataType() == Field.DataType.BOOLEAN || group) {
            type = Align.Type.CENTER;
        } else if (isDataTypeNumericLike(field)) {
            type = header ? Align.Type.CENTER : Align.Type.RIGHT;
        }
        Align alignAnnot = field.findAnnotation(Align.class);
        if (alignAnnot != null) {
            type = alignAnnot.value();
        } else {
            net.microfalx.bootstrap.dataset.annotation.Formattable formattableAnnot = field.findAnnotation(net.microfalx.bootstrap.dataset.annotation.Formattable.class);
            if (formattableAnnot != null && formattableAnnot.alert() != net.microfalx.bootstrap.dataset.annotation.Formattable.AlertProvider.class) {
                type = Align.Type.CENTER;
            }
            Chartable chartableAnnot = field.findAnnotation(Chartable.class);
            if (chartableAnnot != null) type = Align.Type.CENTER;
        }
        return type;
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
        State state = dataSet.getState();
        Object value = field.get(model);
        if (field.getDataType() == Field.DataType.BOOLEAN) {
            if (state == State.VIEW) {
                // the value attribute should be null during view for booleans since we are used checked attribute
                return null;
            } else {
                // the value will use a check/uncheck glyph from FontAwesome
                boolean bool = Boolean.TRUE.equals(value);
                return bool ? BOOLEAN_CHECKED : BOOLEAN_UNCHECKED;
            }
        } else if (state == State.VIEW && value instanceof Temporal) {
            return FormatterUtils.basicFormatting(value);
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
        Object value = field.get(model);
        if (value == null) return null;
        if (dataSet.getState() == State.VIEW || dataSet.getState() == State.BROWSE) {
            return getDisplayValue(model, field);
        } else {
            return ObjectUtils.toString(value);
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
     * Returns the name of the record.
     *
     * @param model the model
     * @return a non-null instance
     */
    public String getName(M model) {
        DataSet<M, F, ID> dataSet = getDataSet();
        return dataSet.getName(model);
    }

    /**
     * Returns the tooltip for the search field.
     *
     * @return the tooltip
     */
    public String getSearchTooltip() {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation(false);
        String resourcePath = null;
        if (dataSetAnnotation != null && isNotEmpty(dataSetAnnotation.queryHelp())) {
            resourcePath = dataSetAnnotation.queryHelp();
        }
        resourcePath = defaultIfEmpty(resourcePath, "/help/dataset/default.html");
        Resource resource = ClassPathResource.file(resourcePath);
        try {
            String content = resource.loadAsString();
            DataSet<M, F, ID> dataSet = getDataSet(false);
            if (dataSet != null) {
                String fieldNames = dataSet.getMetadata().getFields().stream()
                        .filter(dataSet::isFilterable).map(Field::getName)
                        .collect(Collectors.joining(", "));
                content = org.apache.commons.lang3.StringUtils.replaceOnce(content, "${FIELDS}", fieldNames);
            }
            return content;
        } catch (IOException e) {
            LOGGER.error("Failed to create search tooltip for " + resourcePath, e);
            return null;
        }
    }

    /**
     * Called from the template before each model is displayed in the grid.
     *
     * @param model the model
     */
    public void beforeBrowse(M model) {
        DataSet<M, F, ID> dataSet = getDataSet();
        RedirectAttributesModelMap controllerModel = new RedirectAttributesModelMap();
        DataSetController<M, ID> dataSetController = getDataSetController();
        Reflect.on(dataSetController).call("beforeBrowse", dataSet, controllerModel, model);
    }

    /**
     * Returns the models with a given field.
     *
     * @param field the field
     * @return a non-null instance
     */
    public Iterable<Lookup<Object>> getDropDownValues(Field<M> field) {
        Class<?> model = field.getDataClass();
        if (field.getDataType() == Field.DataType.COLLECTION) model = field.getGenericDataClass();
        net.microfalx.bootstrap.dataset.annotation.Lookup lookupAnnot = field.findAnnotation(net.microfalx.bootstrap.dataset.annotation.Lookup.class);
        if (lookupAnnot != null) model = lookupAnnot.model();
        LookupProvider<Lookup<Object>, Object> lookupProvider = dataSetService.getLookupProvider(model);
        Iterable<Lookup<Object>> providerData = lookupProvider.findAll(Pageable.ofSize(5000));
        if (field.isRequired() || field.getDataType().isStructure()) {
            return providerData;
        } else {
            Collection<Lookup<Object>> empty = List.of(new DefaultLookup<>(EMPTY_STRING, EMPTY_STRING));
            return Iterables.concat(empty, providerData);
        }
    }

    /**
     * Returns whether the current lookup is actually pointing to the current value of a model.
     *
     * @param model  the current model
     * @param field  the field which holds another model
     * @param lookup the lookup
     * @return {@code true} if the current value is selected, {@code false} otherwise
     */
    public boolean isSelected(M model, Field<M> field, Lookup<Object> lookup) {
        if (model == null) return false;
        requireNonNull(field);
        Object value = field.get(model);
        if (value == null) {
            return false;
        } else {
            if (isLookupField(field)) {
                value = dataSetService.getId(value);
                return ObjectUtils.equals(value, lookup.getId());
            } else if (field.getDataType() == Field.DataType.COLLECTION) {
                Collection<?> entries = (Collection<?>) value;
                for (Object entry : entries) {
                    value = dataSetService.getId(entry);
                    if (ObjectUtils.equals(value, lookup.getId())) return true;
                }
                return false;
            } else {
                return false;
            }
        }
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

    /**
     * Returns whether the field is supported by a lookup (complex or simple, like an ENUM).
     *
     * @param field the field
     * @return {@code true} if a model behind the field, {@code false} otherwise
     */
    private boolean isLookupField(Field<M> field) {
        return isModelOrEnum(field)
                || field.hasAnnotation(net.microfalx.bootstrap.dataset.annotation.Lookup.class);
    }

    /**
     * Returns whether the field is supported by a lookup (complex or simple, like an ENUM).
     *
     * @param field the field
     * @return {@code true} if a model behind the field, {@code false} otherwise
     */
    private boolean isLookupFieldOrCollection(Field<M> field) {
        return isLookupField(field)
                || field.getDataType() == Field.DataType.COLLECTION;
    }

    /**
     * Returns whether the field holds a number of any type that would result in a number when formatted/rendered.
     *
     * @param field the field
     * @return {@code true} if number like type, {@code false} otherwise
     */
    private boolean isDataTypeNumericLike(Field<M> field) {
        if (field.getDataType().isNumeric()) return true;
        Class<?> dataClass = field.getDataClass();
        return dataClass == Duration.class;
    }

    private static void initializeColors() {
        if (attributeClassesInitialized.compareAndSet(false, true)) {
            synchronized (attributeClassesInitialized) {
                if (attributeClassesQueue.isEmpty() && attributeClasses.isEmpty()) {
                    for (int i = 1; i <= 30; i++) {
                        attributeClassesQueue.offer("name-sample" + i);
                    }
                    try {
                        Collection<Resource> resources = ClassPathResource.files("dataset_attribute_classes.txt").list();
                        for (Resource resource : resources) {
                            Properties properties = new Properties();
                            properties.load(resource.getInputStream());
                            properties.forEach((k, v) -> attributeClasses.put((String) k, (String) v));
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to load attribute classes", e);
                    }
                }
            }
        }
    }

    private Component.Type getComponentType(Field<M> field) {
        Component componentAnnot = field.findAnnotation(Component.class);
        return componentAnnot != null ? componentAnnot.value() : Component.Type.TEXT_FIELD;
    }

    public boolean isModelOrEnum(Field<M> field) {
        return field.getDataType() == Field.DataType.MODEL || field.getDataType() == Field.DataType.ENUM;
    }

    private String getHeaderCustomClasses(Field<M> field) {
        return switch (field.getDataType()) {
            case DATE -> "date-field";
            case TIME -> "time-field";
            case DATE_TIME -> "date-time-field";
            case BOOLEAN -> "boolean-field";
            case ENUM -> "enum-field";
            default -> EMPTY_STRING;
        };
    }

    public static class FormGroup<M, F extends Field<M>, ID> {

        private final String id;
        private final String label;
        private final List<F> fields = new ArrayList<>();
        private boolean active;

        FormGroup(String label) {
            requireNonNull(label);
            this.label = label;
            this.id = toIdentifier(label);
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public boolean isActive() {
            return active;
        }

        public String getDomId() {
            return "#" + getId();
        }

        public String getTabCssClass() {
            return active ? "active" : "";
        }

        public String getContentCssClass() {
            return active ? "show active" : "";
        }

        public boolean isField() {
            return fields.size() == 1;
        }

        public boolean isGroup() {
            return !isField();
        }

        public Collection<F> getFields() {
            return unmodifiableCollection(fields);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", FormGroup.class.getSimpleName() + "[", "]")
                    .add("label='" + label + "'")
                    .add("fields=" + fields)
                    .toString();
        }
    }

    public static class FormGroups<M, F extends Field<M>, ID> {

        private final DataSet<M, F, ID> dataSet;
        private final Collection<F> fields;
        private final List<FormGroup<M, F, ID>> groups = new ArrayList<>();
        private final Map<String, String> groupOverrides = new HashMap<>();

        FormGroups(DataSet<M, F, ID> dataSet, Collection<F> fields) {
            this.dataSet = dataSet;
            this.fields = fields;
            init();
        }

        public boolean hasGroups() {
            return groups.size() > 1;
        }

        public Collection<FormGroup<M, F, ID>> getGroups() {
            return unmodifiableCollection(groups);
        }

        private FormGroup<M, F, ID> findGroup(String label) {
            for (FormGroup<M, F, ID> group : groups) {
                if (group.label.equals(label)) return group;
            }
            return null;
        }

        private void init() {
            Tabs tabs = dataSet.getMetadata().findAnnotation(Tabs.class);
            if (tabs == null) return;
            initOverrides(tabs);
            boolean useLabelAsGroup = tabs.useLabelAsGroup();
            FormGroup<M, F, ID> defaultGroup = new FormGroup<>(tabs.defaultTab());
            defaultGroup.active = true;
            groups.add(defaultGroup);
            for (F field : fields) {
                Tab tabAnnot = field.findAnnotation(Tab.class);
                FormGroup<M, F, ID> group;
                if (tabAnnot != null) {
                    group = findOrCreateGroup(tabAnnot.label());
                } else if (isNotEmpty(field.getGroup()) && useLabelAsGroup) {
                    group = findOrCreateGroup(field.getGroup());
                } else {
                    String label = groupOverrides.get(field.getName().toLowerCase());
                    if (isNotEmpty(label)) {
                        group = findOrCreateGroup(label);
                    } else {
                        group = defaultGroup;
                    }
                }
                group.fields.add(field);
            }
        }

        private FormGroup<M, F, ID> findOrCreateGroup(String label) {
            FormGroup<M, F, ID> group = findGroup(label);
            if (group == null) {
                group = new FormGroup<>(label);
                groups.add(group);
            }
            return group;
        }

        private void initOverrides(Tabs tabs) {
            for (String attribute : tabs.attributes()) {
                groupOverrides.put(attribute.toLowerCase(), "Attributes");
            }
            for (Tab tab : tabs.fields()) {
                if (isEmpty(tab.fieldName())) continue;
                groupOverrides.put(tab.fieldName().toLowerCase(), tab.label());
            }
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", FormGroups.class.getSimpleName() + "[", "]")
                    .add("groups=" + groups)
                    .toString();
        }
    }

    public static class ColumnGroup<M, F extends Field<M>, ID> {

        private final String label;
        private final List<F> fields = new ArrayList<>();

        ColumnGroup(String label) {
            this.label = label;
        }

        ColumnGroup(F field) {
            this.label = field.getLabel();
            this.fields.add(field);
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return getField().getName();
        }

        public boolean isField() {
            return fields.size() == 1;
        }

        public boolean isGroup() {
            return !isField();
        }

        public F getField() {
            return fields.get(0);
        }

        public int getColSpan() {
            return fields.size();
        }

        public int getRowSpan() {
            return fields.size() == 1 ? 2 : 1;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ColumnGroup.class.getSimpleName() + "[", "]")
                    .add("label='" + label + "'")
                    .add("fields=" + fields)
                    .toString();
        }
    }

    public static class ColumnGroups<M, F extends Field<M>, ID> {

        private final Collection<F> fields;
        private final List<ColumnGroup<M, F, ID>> groups = new ArrayList<>();

        ColumnGroups(Collection<F> fields) {
            this.fields = fields;
            init();
        }

        public Iterable<ColumnGroup<M, F, ID>> getFirstRow() {
            return groups;
        }

        public Iterable<ColumnGroup<M, F, ID>> getSecondRow() {
            return groups.stream().filter(g -> g.isGroup())
                    .flatMap(g -> g.fields.stream()).map(f -> new ColumnGroup<M, F, ID>(f)).toList();
        }

        private ColumnGroup<M, F, ID> findGroup(Field<M> field) {
            for (ColumnGroup<M, F, ID> group : groups) {
                if (group.label.equals(field.getGroup())) return group;
            }
            return null;
        }

        private void init() {
            for (F field : fields) {
                ColumnGroup<M, F, ID> group;
                if (isEmpty(field.getGroup())) {
                    group = new ColumnGroup<>(field);
                    groups.add(group);
                } else {
                    group = findGroup(field);
                    if (group == null) {
                        group = new ColumnGroup<>(field.getGroup());
                        groups.add(group);
                    }
                    group.fields.add(field);
                }
            }
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ColumnGroups.class.getSimpleName() + "[", "]")
                    .add("groups=" + groups)
                    .toString();
        }
    }


}
