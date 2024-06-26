package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.template.tools.*;
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
    private final MetadataService metadataService;
    private final DataSetService dataSetService;
    private final HelpService helpService;
    private final ContentService contentService;

    public ExpressionsDialect(ApplicationService applicationService, MetadataService metadataService, DataSetService dataSetService,
                              HelpService helpService, ContentService contentService) {
        super("application");
        this.applicationService = applicationService;
        this.metadataService = metadataService;
        this.dataSetService = dataSetService;
        this.helpService = helpService;
        this.contentService = contentService;
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return EXPRESSION_OBJECTS_FACTORY;
    }

    class ExpressionsObjectFactory implements IExpressionObjectFactory {

        public static final String APPLICATION_OBJECT_NAME = "application";
        public static final String NAVIGATION_OBJECT_NAME = "navigation";
        public static final String COMPONENT_OBJECT_NAME = "component";
        public static final String HELP_OBJECT_NAME = "help";
        public static final String DATASET_OBJECT_NAME = "dataset";
        public static final String LINK_OBJECT_NAME = "link";
        public static final String USER_OBJECT_NAME = "user";
        public static final String RESOURCE_OBJECT_NAME = "resources";
        public static final String MODEL_TOOL_NAME = "models";
        public static final String CONTENT_TOOL_NAME = "content";
        public static final String FORMAT_TOOL_NAME = "formats";

        protected static final Set<String> ALL_EXPRESSION_OBJECT_NAMES = unmodifiableSet(
                new LinkedHashSet<>(asList(APPLICATION_OBJECT_NAME, NAVIGATION_OBJECT_NAME, COMPONENT_OBJECT_NAME,
                        USER_OBJECT_NAME, DATASET_OBJECT_NAME, LINK_OBJECT_NAME, HELP_OBJECT_NAME,
                        RESOURCE_OBJECT_NAME, MODEL_TOOL_NAME, CONTENT_TOOL_NAME, FORMAT_TOOL_NAME)));

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
                return new ComponentTool(context, applicationService);
            } else if (DATASET_OBJECT_NAME.equals(expressionObjectName)) {
                return new DataSetTool<>(context, dataSetService);
            } else if (LINK_OBJECT_NAME.equals(expressionObjectName)) {
                return new LinkTool(context);
            } else if (RESOURCE_OBJECT_NAME.equals(expressionObjectName)) {
                return new ResourceTool(context);
            } else if (MODEL_TOOL_NAME.equals(expressionObjectName)) {
                return new ModelTool(context, metadataService);
            } else if (CONTENT_TOOL_NAME.equals(expressionObjectName)) {
                return new ContentTool(context, contentService);
            } else if (FORMAT_TOOL_NAME.equals(expressionObjectName)) {
                return new FormatTool(context);
            } else if (HELP_OBJECT_NAME.equals(expressionObjectName)) {
                return new HelpTool(context, helpService);
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
