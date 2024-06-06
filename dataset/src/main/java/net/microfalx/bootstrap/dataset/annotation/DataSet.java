package net.microfalx.bootstrap.dataset.annotation;

import net.microfalx.bootstrap.dataset.DataSetUtils;
import net.microfalx.bootstrap.model.Metadata;

import java.lang.annotation.*;

/**
 * An annotation used to provide information about a data set.
 * <p>
 * The template specific properties are passed directly to the template engine, and they are specific to
 * each template engine. However, the Data Set uses conventions present in <i>Thymeleaf</i> rendering engine
 * since it is the default template engine.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSet {

    /**
     * Returns the model for the data set model.
     *
     * @return the model class, Object is not set
     */
    Class<?> model() default Object.class;

    /**
     * Returns whether the data set will have virtual scrolling.
     *
     * @return {@code true} for virtual scrolling, {@code false} otherwise
     */
    boolean virtual() default true;

    /**
     * Returns whether the query should not be parsed.
     * <p>
     * If the query is not parsed, it is set into the filter under a field called "query".
     *
     * @return {@code true} to pass the raw query, {@code false} to parse the query and convert to expressions
     */
    boolean rawQuery() default false;

    /**
     * Returns the default page size.
     *
     * @return a non-null instance
     */
    int pageSize() default 25;

    /**
     * Returns whether the data set allows the user to upload a file to add a new model.
     * <p>
     * When disabled, the user can use the add action (if enabled) to manually insert a new model.
     *
     * @return {@code true} to allow for download,  {@code false} otherwise
     */
    boolean canUpload() default false;

    /**
     * Returns whether the data set allows the user to download a file from an existing model.
     * <p>
     * When disabled, the user can use the add action (if enabled) to manually insert a new entry.
     *
     * @return {@code true} to allow for upload,  {@code false} otherwise
     */
    boolean canDownload() default false;

    /**
     * Returns whether the data set allows the user to add manually a new entry.
     * <p>
     * When disabled, usually the user can either upload a file to provision a new entry (or a set of entries) or
     * the user is only allowed to edit.
     *
     * @return {@code true} to allow for add,  {@code false} otherwise
     */
    boolean canAdd() default true;

    /**
     * Returns whether the data set allows the user to remove entries.
     * <p>
     * When disabled, usually the user can either upload a file to provision a new entry (or a set of entries) or
     * the user is only allowed to edit.
     *
     * @return {@code true} to allow for delete,  {@code false} otherwise
     */
    boolean canDelete() default true;

    /**
     * Returns the default time range.
     * <p>
     * By default, if the data set has a time component, a default range will be used using todays date.
     *
     * @return zero (no default range), one (one day) or two (a range)
     */
    String[] range() default {"today"};

    /**
     * Returns the operator injected when the user clicks on a field value in the grid.
     *
     * @return the operator
     */
    String filterOperator() default DataSetUtils.DEFAULT_FILTER_OPERATOR;

    /**
     * Returns the operator injected when the user clicks on a field value in the grid.
     *
     * @return the operator
     */
    char filterQuoteChar() default DataSetUtils.DEFAULT_FILTER_QUOTE_CHAR;

    /**
     * Returns a resource path used to build the help (tooltip) for the search box
     *
     * @return the resource path, empty to use the default help
     */
    String queryHelp() default "";

    /**
     * Returns the default query for this data set.
     *
     * @return the query, empty if no default query
     */
    String defaultQuery() default "";

    /**
     * Returns the template to be used to render the details about a model in the grid.
     * <p>
     * The template can have a fragment attached to it, separated by {@code ::}. If the fragment is missing, it defaults
     * to {@code fields}.
     *
     * @return the view, empty if there are no details
     */
    String detailTemplate() default "";

    /**
     * Returns the template to be used to render the model when the "view" action is executed.
     * <p>
     * The template can have a fragment attached to it, separated by {@code ::}. If the fragment is missing, it defaults
     * * to {@code fields}.
     *
     * @return the view, empty to use default view
     */
    String viewTemplate() default "";

    /**
     * Returns an array of CSS classes to be applied to the view modal.
     *
     * @return the classes
     */
    String[] viewClasses() default "";

    /**
     * Returns whether the data set will be filtered by time, based on one of the available timestamp fields.
     *
     * @return {@code true} to filter, {@code false} to show all records
     * @see Metadata#findTimestampField()
     */
    boolean timeFilter() default true;

    /**
     * Returns whether the data set can display trends.
     *
     * @return {@code true} if trends can be displayed, {@code false} otherwise
     */
    boolean trend() default false;

    /**
     * Returns a list of fields to trend.
     *
     * @return an array of field names, empty to leave the data set provide the field names.
     */
    String[] trendFieldNames() default {};
}
