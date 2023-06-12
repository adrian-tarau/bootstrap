package net.microfalx.bootstrap.web.template;

import net.microfalx.bootstrap.web.application.ApplicationService;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashSet;
import java.util.Set;

public class AssetDialect implements IProcessorDialect {

    private static final String DIALECT_PREFIX = "asset";
    private static final String DIALECT_NAME = "Bootstrap Asset";
    private static final int PRECEDENCE = 1000;

    private ApplicationService applicationService;

    public AssetDialect(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public String getName() {
        return DIALECT_NAME;
    }

    public String getPrefix() {
        return DIALECT_PREFIX;
    }

    public int getDialectProcessorPrecedence() {
        return PRECEDENCE;
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new ScriptsAssetTag());
        processors.add(new StylesheetsAssetTag());
        return processors;
    }

    private abstract class BaseAssetTag extends AbstractElementTagProcessor {

        public BaseAssetTag(String elementName) {
            super(TemplateMode.HTML, DIALECT_PREFIX, elementName, true, null, false, PRECEDENCE);
        }
    }

    private class ScriptsAssetTag extends BaseAssetTag {

        public ScriptsAssetTag() {
            super("js");
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, IElementTagStructureHandler structureHandler) {
            structureHandler.replaceWith(applicationService.getScripts(tag.getCol() - 1), false);
        }
    }

    private class StylesheetsAssetTag extends BaseAssetTag {

        public StylesheetsAssetTag() {
            super("css");
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, IElementTagStructureHandler structureHandler) {
            structureHandler.replaceWith(applicationService.getStylesheets(tag.getCol() - 1), false);
        }

    }
}
