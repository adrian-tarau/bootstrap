package net.microfalx.bootstrap.web.template;

import net.microfalx.lang.Descriptable;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.springdata.util.Expressions;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;
import org.unbescape.html.HtmlEscape;

import java.util.HashSet;
import java.util.Set;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Processors for {@link net.microfalx.bootstrap.web.component.Component}.
 */
public class ComponentDialect extends AbstractProcessorDialect {

    private static final String DIALECT_PREFIX = "component";
    private static final String DIALECT_NAME = "Bootstrap Component";
    private static final int PRECEDENCE = 1000;
    private static final int TOOLTIP_SHOW_DELAY = 2000;

    public ComponentDialect(ApplicationContext applicationContext) {
        super(DIALECT_NAME, DIALECT_PREFIX, PRECEDENCE, applicationContext);
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> processors = new HashSet<>();
        processors.add(new RenderTagProcessor());
        processors.add(new TooltipAttributeProcessor());
        processors.add(new TooltipTextAttributeProcessor());
        processors.add(new TooltipPlacementAttributeProcessor());
        processors.add(new TooltipDelayAttributeProcessor());
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

    private class TooltipAttributeProcessor extends BaseAttributeProcessor {

        public TooltipAttributeProcessor() {
            super("tooltip");
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
            Object value = null;
            String title = null;
            if (isNotEmpty(attributeValue)) value = Expressions.evaluate(context, attributeValue);
            if (value instanceof Descriptable) {
                title = ((Descriptable) value).getDescription();
            } else if (value instanceof String) {
                title = (String) value;
            }
            if (isNotEmpty(title)) {
                structureHandler.setAttribute("data-tippy-content", HtmlEscape.escapeHtml4Xml(title));
            }
        }
    }

    private class TooltipPlacementAttributeProcessor extends BaseAttributeProcessor {

        public TooltipPlacementAttributeProcessor() {
            super("tooltip-placement");
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
            if (isNotEmpty(attributeValue)) {
                structureHandler.setAttribute("data-tippy-placement", attributeValue);
            }
        }
    }

    private class TooltipDelayAttributeProcessor extends BaseAttributeProcessor {

        public TooltipDelayAttributeProcessor() {
            super("tooltip-delay");
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
            if (isNotEmpty(attributeValue)) {
                structureHandler.setAttribute("data-tippy-delay", attributeValue);
            }
        }
    }

    private class TooltipTextAttributeProcessor extends BaseAttributeProcessor {

        public TooltipTextAttributeProcessor() {
            super("tooltip-text");
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
            if (isNotEmpty(attributeValue)) {
                structureHandler.setAttribute("data-tippy-content", HtmlEscape.escapeHtml4Xml(attributeValue));
            }
        }
    }


}
