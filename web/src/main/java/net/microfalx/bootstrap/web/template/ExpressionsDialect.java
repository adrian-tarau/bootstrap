package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.chart.ChartService;
import net.microfalx.bootstrap.web.template.tools.*;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class ExpressionsDialect extends AbstractDialect implements IExpressionObjectDialect {

    private final IExpressionObjectFactory EXPRESSION_OBJECTS_FACTORY = new ExpressionsObjectFactory();

    private final ApplicationContext applicationContext;
    private final ApplicationService applicationService;
    private final MetadataService metadataService;
    private final DataSetService dataSetService;
    private final HelpService helpService;
    private final ContentService contentService;
    private final ChartService chartService;

    public ExpressionsDialect(ApplicationContext applicationContext) {
        super("application");
        requireNonNull(applicationContext);
        this.applicationContext = applicationContext;
        this.applicationService = applicationContext.getBean(ApplicationService.class);
        this.metadataService = applicationContext.getBean(MetadataService.class);
        this.dataSetService = applicationContext.getBean(DataSetService.class);
        this.helpService = applicationContext.getBean(HelpService.class);
        this.contentService = applicationContext.getBean(ContentService.class);
        this.chartService = applicationContext.getBean(ChartService.class);
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return EXPRESSION_OBJECTS_FACTORY;
    }

    class ExpressionsObjectFactory implements IExpressionObjectFactory {

        public static final String APPLICATION_OBJECT_NAME = "application";
        public static final String NAVIGATION_OBJECT_NAME = "navigation";
        public static final String PAGE_OBJECT_NAME = "page";
        public static final String SECURITY_OBJECT_NAME = "security";
        public static final String COMPONENT_OBJECT_NAME = "component";
        public static final String HELP_OBJECT_NAME = "help";
        public static final String DATASET_OBJECT_NAME = "dataset";
        public static final String BOOTSTRAP_OBJECT_NAME = "bootstrap";
        public static final String LINK_OBJECT_NAME = "link";
        public static final String USER_OBJECT_NAME = "user";
        public static final String RESOURCE_OBJECT_NAME = "resources";
        public static final String MODEL_TOOL_NAME = "models";
        public static final String CONTENT_TOOL_NAME = "content";
        public static final String FORMAT_TOOL_NAME = "formats";

        protected static final Set<String> ALL_EXPRESSION_OBJECT_NAMES = unmodifiableSet(
                new LinkedHashSet<>(asList(APPLICATION_OBJECT_NAME, NAVIGATION_OBJECT_NAME, PAGE_OBJECT_NAME,
                        SECURITY_OBJECT_NAME, COMPONENT_OBJECT_NAME, USER_OBJECT_NAME, DATASET_OBJECT_NAME,
                        LINK_OBJECT_NAME, HELP_OBJECT_NAME, RESOURCE_OBJECT_NAME, MODEL_TOOL_NAME,
                        CONTENT_TOOL_NAME, FORMAT_TOOL_NAME, BOOTSTRAP_OBJECT_NAME)));

        @Override
        public Set<String> getAllExpressionObjectNames() {
            return ALL_EXPRESSION_OBJECT_NAMES;
        }

        @Override
        public Object buildObject(IExpressionContext context, String expressionObjectName) {
            if (APPLICATION_OBJECT_NAME.equals(expressionObjectName)) {
                return applicationService.getApplication();
            } else if (NAVIGATION_OBJECT_NAME.equals(expressionObjectName)) {
                return new NavigationTool(context, applicationContext);
            } else if (COMPONENT_OBJECT_NAME.equals(expressionObjectName)) {
                return new ComponentTool(context, applicationContext);
            } else if (DATASET_OBJECT_NAME.equals(expressionObjectName)) {
                return new DataSetTool<>(context, applicationContext);
            } else if (LINK_OBJECT_NAME.equals(expressionObjectName)) {
                return new LinkTool(context, applicationContext);
            } else if (RESOURCE_OBJECT_NAME.equals(expressionObjectName)) {
                return new ResourceTool(context, applicationContext);
            } else if (MODEL_TOOL_NAME.equals(expressionObjectName)) {
                return new ModelTool(context, applicationContext);
            } else if (CONTENT_TOOL_NAME.equals(expressionObjectName)) {
                return new ContentTool(context, applicationContext);
            } else if (FORMAT_TOOL_NAME.equals(expressionObjectName)) {
                return new FormatTool(context, applicationContext);
            } else if (HELP_OBJECT_NAME.equals(expressionObjectName)) {
                return new HelpTool(context, applicationContext);
            } else if (USER_OBJECT_NAME.equals(expressionObjectName)) {
                return TemplateSecurityContext.get(context);
            } else if (SECURITY_OBJECT_NAME.equals(expressionObjectName)) {
                return new SecurityTool(context, applicationContext);
            } else if (PAGE_OBJECT_NAME.equals(expressionObjectName)) {
                return new PageTool(context, applicationContext);
            } else if (BOOTSTRAP_OBJECT_NAME.equals(expressionObjectName)) {
                return new BootstrapTool(context, applicationContext);
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
