package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.resource.MimeType;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.IContext;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class ContentTool extends AbstractTool {

    private static final String VIEW_ACTION = "view";
    private static final String EDIT_ACTION = "edit";

    private final ContentService contentService;
    private final LinkTool linkTool;

    public ContentTool(IContext context, ContentService contentService) {
        super(context);
        this.contentService = contentService;
        this.linkTool = new LinkTool(context);
    }

    /**
     * Registers the content and creates a link to a document to load the content in view mode.
     *
     * @param content the content
     * @return the link
     */
    public String getViewUri(Content content) {
        return getSrc(VIEW_ACTION, content);
    }

    /**
     * Registers the content and creates a link to a document to load the content in edit mode.
     *
     * @param content the content
     * @return the link
     */
    public String getEditUri(Content content) {
        return getSrc(EDIT_ACTION, content);
    }

    /**
     * Abbreviates the end of a String using ellipses.
     *
     * @param value    the value
     * @param maxWidth the maximum width
     * @return the abbreviated string
     */
    public String abbreviate(String value, int maxWidth) {
        return StringUtils.abbreviate(value, maxWidth);
    }

    /**
     * Abbreviates the middle a String using ellipses.
     *
     * @param value    the value
     * @param maxWidth the maximum width
     * @return the abbreviated string
     */
    public String abbreviateMiddle(String value, int maxWidth) {
        return StringUtils.abbreviateMiddle(value, "...", maxWidth);
    }

    private boolean needsEditor(Content content) {
        String mimeType = content.getMimeType();
        return !(MimeType.TEXT_PLAIN.equals(mimeType) || MimeType.TEXT_HTML.equals(mimeType)
                || MimeType.APPLICATION_OCTET_STREAM.equals(mimeType));
    }

    private String getSrc(String action, Content content) {
        requireNonNull(content);
        String id = contentService.registerContent(content);
        if (VIEW_ACTION.equals(action) && !needsEditor(content)) {
            return "/content/get/" + id;
        } else {
            return "/content/" + action + "/" + id;
        }
    }
}
