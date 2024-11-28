package net.microfalx.bootstrap.web.util;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.resource.Resource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A tool around code editor.
 */
public class CodeEditor<ID> {

    private final ContentService contentService;
    private final Resource resource;
    private final Object controller;
    private final String path;

    public CodeEditor(ContentService contentService, Resource resource, Object controller) {
        requireNonNull(contentService);
        requireNonNull(resource);
        requireNonNull(controller);
        this.contentService = contentService;
        this.resource = resource;
        this.controller = controller;
        this.path = extractPath();
    }

    /**
     * Prepares the model with the required context and returns the template which will render
     * the editor.
     *
     * @param id    the identifier
     * @param model the controller model
     * @return the template fragment
     */
    public String getEditorDialog(ID id, Model model) {
        requireNonNull(contentService);
        Content content = Content.create(resource);
        contentService.registerContent(content);
        model.addAttribute("resource", resource);
        model.addAttribute("content", content);
        model.addAttribute("path", path);
        model.addAttribute("id", id);
        return "misc/editor::#editor-modal";
    }

    private String extractPath() {
        RequestMapping requestMappingAnnot = AnnotationUtils.getAnnotation(controller, RequestMapping.class);
        if (requestMappingAnnot == null)
            throw new IllegalArgumentException("Object '" + ClassUtils.getName(controller) + "' is not a controller");
        if (ObjectUtils.isNotEmpty(requestMappingAnnot.path())) {
            return requestMappingAnnot.path()[0];
        } else if (ObjectUtils.isNotEmpty(requestMappingAnnot.value())) {
            return requestMappingAnnot.value()[0];
        } else {
            throw new IllegalArgumentException("Controller '" + ClassUtils.getName(controller) + "' has no paths");
        }
    }
}
