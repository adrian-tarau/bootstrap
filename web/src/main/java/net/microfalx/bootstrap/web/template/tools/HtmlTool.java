package net.microfalx.bootstrap.web.template.tools;

import org.thymeleaf.context.IContext;

/**
 * Template utilities around HTML.
 */
@SuppressWarnings("unused")
public class HtmlTool extends AbstractTool {

    public HtmlTool(IContext context) {
        super(context);
    }

    public HtmlBuilder empty() {
        return new HtmlBuilder();
    }

    public HtmlBuilder div() {
        return new HtmlBuilder("div");
    }

    public HtmlBuilder span() {
        return new HtmlBuilder("span");
    }

}
