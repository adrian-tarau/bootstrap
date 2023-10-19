package net.microfalx.bootstrap.web.template;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashSet;
import java.util.Set;

/**
 * Processors for {@link net.microfalx.bootstrap.web.component.Component}.
 */
public class ComponentDialect extends AbstractProcessorDialect {

    private static final String DIALECT_PREFIX = "component";
    private static final String DIALECT_NAME = "Bootstrap Component";
    private static final int PRECEDENCE = 1000;

    public ComponentDialect() {
        super(DIALECT_NAME, DIALECT_PREFIX, PRECEDENCE);
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> processors = new HashSet<>();
        processors.add(new RenderTagProcessor());
        return processors;
    }

    private abstract class BaseTagProcessor extends AbstractElementTagProcessor {

        public BaseTagProcessor(String elementName) {
            super(TemplateMode.HTML, DIALECT_PREFIX, elementName, true, null, false, PRECEDENCE);
        }

    }

    private abstract class BaseAttributeProcessor extends AbstractAttributeTagProcessor {

        public BaseAttributeProcessor(String attributeName) {
            super(TemplateMode.HTML, DIALECT_PREFIX, null, true, attributeName, true, PRECEDENCE, true);
        }

    }

    private class RenderTagProcessor extends BaseTagProcessor {

        public RenderTagProcessor() {
            super("render");
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, IElementTagStructureHandler structureHandler) {
            System.out.println("Stop");
        }
    }

    private class ActionableRenderTagProcessor extends BaseTagProcessor {

        public ActionableRenderTagProcessor() {
            super("action");
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, IElementTagStructureHandler structureHandler) {
            System.out.println("Stop");
        }
    }


}
