package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.bootstrap.web.chart.ChartException;
import net.microfalx.bootstrap.web.chart.ChartService;
import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.springdata.util.Expressions;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;
import org.unbescape.html.HtmlEscape;

import java.util.HashSet;
import java.util.Set;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Processors for {@link net.microfalx.bootstrap.web.chart.Chart}.
 */
public class ChartDialect extends AbstractProcessorDialect {

    private static final String DIALECT_PREFIX = "chart";
    private static final String DIALECT_NAME = "Bootstrap Chart";
    private static final int PRECEDENCE = 1000;

    private final ChartService chartService;

    public ChartDialect(ChartService chartService) {
        super(DIALECT_NAME, DIALECT_PREFIX, PRECEDENCE);
        ArgumentUtils.requireNonNull(chartService);
        this.chartService = chartService;
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

    private class RenderTagProcessor extends BaseTagProcessor {

        public RenderTagProcessor() {
            super("render");
        }

        private void updateStyles(StringBuilder builder, Chart chart) {
            String width = chart.getOptions().getWidth();
            String height = chart.getOptions().getHeight();
            if (width != null || height != null) {
                builder.append(" style='");
                StringBuilder styleBuilder = new StringBuilder();
                if (width != null) styleBuilder.append("min-width: ").append(width);
                if (height != null) {
                    StringUtils.append(styleBuilder, "min-height: ", ';');
                    styleBuilder.append(height);
                }
                builder.append(styleBuilder);
                builder.append('\'');
            }
        }

        private void updateTooltip(StringBuilder builder, Chart chart) {
            if (isNotEmpty(chart.getDescription())) {
                builder.append(" data-tippy-content='").append(HtmlEscape.escapeHtml4Xml(chart.getDescription())).append("'");
            }
        }

        private String getHtml(Chart chart, boolean inline) {
            StringBuilder builder = new StringBuilder();
            if (inline) {
                builder.append("<span id='").append(chart.getId()).append("' class='chart-container'");
                updateStyles(builder, chart);
                builder.append("></span>\n");
            } else {
                builder.append("<div class='card' id='").append(chart.getId()).append("_card").append("'>\n");
                if (isNotEmpty(chart.getName())) {
                    builder.append("  <div class='card-header'");
                    updateTooltip(builder, chart);
                    builder.append(">").append(chart.getName()).append("</div>\n");
                }
                builder.append("<div id='").append(chart.getId()).append("' class='card-body chart-container'");
                updateStyles(builder, chart);
                builder.append("></div>\n");
                builder.append("</div>");
            }
            return builder.toString();
        }

        private boolean isInline(ITemplateContext context, IProcessableElementTag tag) {
            boolean inline = false;
            IAttribute inlineAttr = tag.getAttribute("inline");
            if (inlineAttr != null) {
                Object value = Expressions.evaluate(context, inlineAttr.getValue());
                inline = StringUtils.asBoolean(value, false);
            }
            return inline;
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, IElementTagStructureHandler structureHandler) {
            IAttribute idAttr = tag.getAttribute("id");
            Chart chart;
            if (idAttr != null) {
                chart = chartService.get(idAttr.getValue());
            } else {
                IAttribute valueAttr = tag.getAttribute("value");
                Object value = Expressions.evaluate(context, valueAttr.getValue());
                if (value instanceof String) {
                    chart = chartService.get((String) value);
                } else if (value instanceof Chart) {
                    chart = (Chart) value;
                } else {
                    throw new ChartException("Expecting a Chart or a chart identifier, got " + ClassUtils.getName(value));
                }
            }
            chartService.register(chart);
            boolean inline = isInline(context, tag);
            structureHandler.replaceWith(getHtml(chart, inline), false);
        }
    }
}
