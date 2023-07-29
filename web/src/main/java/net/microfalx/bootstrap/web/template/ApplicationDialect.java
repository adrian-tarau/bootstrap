package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.web.template.tools.LinkTool;
import net.microfalx.lang.TextUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashSet;
import java.util.Set;

/**
 * Processors for {@link net.microfalx.bootstrap.web.application.Application}.
 */
public class ApplicationDialect extends AbstractProcessorDialect {

    private static final String DIALECT_PREFIX = "application";
    private static final String DIALECT_NAME = "Bootstrap Application";
    private static final int PRECEDENCE = 1000;

    public ApplicationDialect() {
        super(DIALECT_NAME, DIALECT_PREFIX, PRECEDENCE);
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
            StringBuilder builder = new StringBuilder();
            builder.append(SCRIPT_START_TAG);
            builder.append("\nconst REQUEST_PATH=\"").append(linkTool.getSelf()).append("\";");
            builder.append("\nconst REQUEST_QUERY=").append(linkTool.toJson(linkTool.getQuery())).append(";");
            builder.append('\n').append(SCRIPT_START_END);
            structureHandler.replaceWith(TextUtils.insertSpaces(builder.toString(), tag.getCol()), false);
        }
    }

    private static String SCRIPT_START_TAG = "<script type=\"text/javascript\">";
    private static String SCRIPT_START_END = "</script>";
}
