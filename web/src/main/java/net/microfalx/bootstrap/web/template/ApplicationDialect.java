package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.search.SearchUtils;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.template.tools.DataSetTool;
import net.microfalx.bootstrap.web.template.tools.LinkTool;
import net.microfalx.lang.TextUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashSet;
import java.util.Set;

import static net.microfalx.lang.StringUtils.defaultIfEmpty;

/**
 * Processors for {@link net.microfalx.bootstrap.web.application.Application}.
 */
public class ApplicationDialect extends AbstractProcessorDialect {

    private static final String DIALECT_PREFIX = "application";
    private static final String DIALECT_NAME = "Bootstrap Application";
    private static final int PRECEDENCE = 1000;

    private ApplicationService applicationService;
    private DataSetService dataSetService;

    public ApplicationDialect(ApplicationService applicationService, DataSetService dataSetService) {
        super(DIALECT_NAME, DIALECT_PREFIX, PRECEDENCE);
        this.applicationService = applicationService;
        this.dataSetService = dataSetService;
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> processors = new HashSet<>();
        processors.add(new ContextTagProcessor());
        return processors;
    }

    private abstract class BaseTagProcessor extends AbstractElementTagProcessor {

        public BaseTagProcessor(String elementName) {
            super(TemplateMode.HTML, DIALECT_PREFIX, elementName, true, null, false, PRECEDENCE);
        }

    }

    private class ContextTagProcessor extends BaseTagProcessor {

        public ContextTagProcessor() {
            super("context");
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, IElementTagStructureHandler structureHandler) {
            LinkTool linkTool = new LinkTool(context);
            DataSetTool dataSetTool = new DataSetTool(context, dataSetService);
            StringBuilder builder = new StringBuilder();
            builder.append(SCRIPT_START_TAG);
            builder.append("\nconst APP_REQUEST_PATH=\"").append(linkTool.getSelf()).append("\";");
            builder.append("\nconst APP_REQUEST_QUERY=").append(linkTool.toJson(linkTool.getQuery())).append(";");
            String filterableOperator = defaultIfEmpty(dataSetTool.getFilterableOperator(), SearchUtils.DEFAULT_FILTER_OPERATOR);
            builder.append("\nconst DATASET_FILTERABLE_OPERATOR=\"").append(filterableOperator).append("\"");
            String filterableQuoteChar = defaultIfEmpty(dataSetTool.getFilterableQuoteChar(), String.valueOf(SearchUtils.DEFAULT_FILTER_QUOTE_CHAR));
            builder.append("\nconst DATASET_FILTERABLE_QUOTE_CHAR=\"").append(StringEscapeUtils.escapeEcmaScript(filterableQuoteChar)).append("\"");
            builder.append('\n').append(SCRIPT_START_END);
            structureHandler.replaceWith(TextUtils.insertSpaces(builder.toString(), tag.getCol()), false);

        }
    }

    private static String SCRIPT_START_TAG = "<script type=\"text/javascript\">";
    private static String SCRIPT_START_END = "</script>";
}
