package net.microfalx.bootstrap.dataset.annotation;

import java.lang.annotation.*;

/**
 * An annotation used to provide information about a data set.
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
     * Returns the default page size.
     *
     * @return a non-null instance
     */
    int pageSize() default 50;

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
     * Returns the template to be used to render the data instead of the default one.
     *
     * @return the view, empty if not set
     */
    String viewTemplate() default "";

    /**
     * Returns the view to be used to render the data instead of the default one.
     *
     * @return the view, empty if not set
     */
    String viewFragment() default "";

    /**
     * Returns an array of CSS classes to be applied to the view modal.
     *
     * @return the classes
     */
    String[] viewClasses() default "";
}
