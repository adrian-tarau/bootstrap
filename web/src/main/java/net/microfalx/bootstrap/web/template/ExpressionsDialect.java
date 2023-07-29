package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.template.tools.ComponentTool;
import net.microfalx.bootstrap.web.template.tools.DataSetTool;
import net.microfalx.bootstrap.web.template.tools.LinkTool;
import net.microfalx.bootstrap.web.template.tools.NavigationTool;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public class ExpressionsDialect extends AbstractDialect implements IExpressionObjectDialect {

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

    class ExpressionsObjectFactory implements IExpressionObjectFactory {

        public static final String APPLICATION_OBJECT_NAME = "application";
        public static final String NAVIGATION_OBJECT_NAME = "navigation";
        public static final String COMPONENT_OBJECT_NAME = "component";
        public static final String DATASET_OBJECT_NAME = "dataset";
        public static final String LINK_OBJECT_NAME = "link";
        public static final String USER_OBJECT_NAME = "user";

        protected static final Set<String> ALL_EXPRESSION_OBJECT_NAMES = unmodifiableSet(
                new LinkedHashSet<>(asList(APPLICATION_OBJECT_NAME, NAVIGATION_OBJECT_NAME, COMPONENT_OBJECT_NAME,
                        USER_OBJECT_NAME, DATASET_OBJECT_NAME, LINK_OBJECT_NAME)));

        @Override
        public Set<String> getAllExpressionObjectNames() {
            return ALL_EXPRESSION_OBJECT_NAMES;
        }

        @Override
        public Object buildObject(IExpressionContext context, String expressionObjectName) {
            if (APPLICATION_OBJECT_NAME.equals(expressionObjectName)) {
                return applicationService.getApplication();
            } else if (NAVIGATION_OBJECT_NAME.equals(expressionObjectName)) {
                return new NavigationTool(context, applicationService);
            } else if (COMPONENT_OBJECT_NAME.equals(expressionObjectName)) {
                return new ComponentTool(context);
            } else if (DATASET_OBJECT_NAME.equals(expressionObjectName)) {
                return new DataSetTool<>(context);
            } else if (LINK_OBJECT_NAME.equals(expressionObjectName)) {
                return new LinkTool(context);
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
