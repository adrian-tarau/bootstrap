package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.component.Actionable;
import net.microfalx.bootstrap.web.component.Container;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.dataset.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetException;
import org.springframework.data.domain.Page;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class ExpressionsDialect extends AbstractDialect implements IExpressionObjectDialect {

    public static final String BOOLEAN_CHECKED = "<i class=\"far fa-check-square\"></i>";
    public static final String BOOLEAN_UNCHECKED = "<i class=\"far fa-square\"></i>";

    private final IExpressionObjectFactory EXPRESSION_OBJECTS_FACTORY = new ExpressionsObjectFactory();

    private final ApplicationService applicationService;

    public ExpressionsDialect(ApplicationService applicationService) {
        super("application");
        this.applicationService = applicationService;
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return EXPRESSION_OBJECTS_FACTORY;
    }

    /**
     * Returns an attribute of the model.
     *
     * @param context the template context
     * @param name    the attribute name
     * @return the value, null if does not exist
     */
    static <T> T getModelAttribute(IContext context, String name) {
        if (context instanceof IWebContext) {
            return (T) ((IWebContext) context).getExchange().getAttributeValue(name);
        } else {
            return null;
        }
    }

    /**
     * Template utilities around navigation.
     */
    public class NavigationTools {

        /**
         * Returns the navigation with a given identifier.
         *
         * @param id the identifier
         * @return a non-null instance
         */
        public Menu get(String id) {
            return applicationService.getNavigation(id);
        }

    }

    /**
     * Template utilities around components.
     */
    public static class ComponentTools {

        private final IContext context;

        public ComponentTools(IContext context) {
            this.context = context;
        }

        /**
         * Returns a list of children which should be displayed to the current user.
         *
         * @param component the component
         * @return a non-null instance
         */
        public Collection<net.microfalx.bootstrap.web.component.Component<?>> getChildren(net.microfalx.bootstrap.web.component.Component<?> component) {
            TemplateSecurityContext securityContext = TemplateSecurityContext.get();
            if (!(component instanceof Container)) return Collections.emptyList();
            Collection<net.microfalx.bootstrap.web.component.Component<?>> children = new ArrayList<>();
            for (net.microfalx.bootstrap.web.component.Component<?> child : ((Container<?>) component).getChildren()) {
                if (child instanceof Actionable) {
                    Actionable<?> actionable = (Actionable<?>) child;
                    if (actionable.getRoles().isEmpty()) {
                        children.add(child);
                    } else if (securityContext.hasRoles(actionable.getRoles())) {
                        children.add(child);
                    }
                } else {
                    children.add(child);
                }
            }
            return children;
        }

        /**
         * Returns whether the component is a container.
         *
         * @param component the component to validate
         * @return {@code true} if container, {@code false} otherwise
         */
        public boolean isContainer(net.microfalx.bootstrap.web.component.Component<?> component) {
            return component instanceof Container;
        }
    }

    /**
     * Template utilities around data sets
     */
    public static class DataSetTools<M, F extends Field<M>, ID> {

        private final IContext context;

        public DataSetTools(IContext context) {
            this.context = context;
        }

        /**
         * Returns the current data set.
         *
         * @return a non-null instance
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public DataSet<M, F, ID> getDataSet() {
            DataSet dataset = (DataSet) getModelAttribute(context, "dataset");
            if (dataset == null) throw new DataSetException("A data set is not available in the context");
            return dataset;
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
         * Returns the class to be used with  a cell.
         *
         * @param field the field
         * @return the class
         */
        public String getFieldClass(Field<M> field) {
            if (field.getDataType() == Field.DataType.BOOLEAN) {
                return "text-center";
            } else if (field.getDataType().isNumeric()) {
                return "text-right";
            } else {
                return null;
            }
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


    class ExpressionsObjectFactory implements IExpressionObjectFactory {

        public static final String APPLICATION_OBJECT_NAME = "application";
        public static final String NAVIGATION_OBJECT_NAME = "navigation";
        public static final String COMPONENT_OBJECT_NAME = "component";
        public static final String DATASET_OBJECT_NAME = "dataset";
        public static final String USER_OBJECT_NAME = "user";

        protected static final Set<String> ALL_EXPRESSION_OBJECT_NAMES = unmodifiableSet(new LinkedHashSet<>(asList(APPLICATION_OBJECT_NAME, NAVIGATION_OBJECT_NAME, COMPONENT_OBJECT_NAME, USER_OBJECT_NAME, DATASET_OBJECT_NAME)));

        @Override
        public Set<String> getAllExpressionObjectNames() {
            return ALL_EXPRESSION_OBJECT_NAMES;
        }

        @Override
        public Object buildObject(IExpressionContext context, String expressionObjectName) {
            if (APPLICATION_OBJECT_NAME.equals(expressionObjectName)) {
                return applicationService.getApplication();
            } else if (NAVIGATION_OBJECT_NAME.equals(expressionObjectName)) {
                return new NavigationTools();
            } else if (COMPONENT_OBJECT_NAME.equals(expressionObjectName)) {
                return new ComponentTools(context);
            } else if (DATASET_OBJECT_NAME.equals(expressionObjectName)) {
                return new DataSetTools<>(context);
            } else if (USER_OBJECT_NAME.equals(expressionObjectName)) {
                return TemplateSecurityContext.get(context);
            } else {
                return null;
            }
        }

        @Override
        public boolean isCacheable(String expressionObjectName) {
            return true;
        }
    }
}
