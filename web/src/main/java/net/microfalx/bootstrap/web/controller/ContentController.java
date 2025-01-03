package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.resource.Resource.FILE_NAME_ATTR;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.parseMediaType;

@RequestMapping("/content")
@Controller
public class ContentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentController.class);
    private static final Map<String, String> MODES = new HashMap<>();

    @Autowired
    private ContentService contentService;

    @GetMapping("/get/{id}")
    public ResponseEntity<Object> get(Model model, @PathVariable("id") String id,
                                      @RequestParam(value = "download", required = false) Boolean download) {
        download = ObjectUtils.defaultIfNull(download, Boolean.FALSE);
        try {
            Content content = contentService.getContent(id);
            if (!content.exists()) return ResponseEntity.notFound().build();
            Resource resource = contentService.view(content);
            ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok().contentType(parseMediaType(resource.getMimeType()));
            if (!MimeType.get(resource.getMimeType()).isText() || download) {
                String fileName = content.getResource().getAttribute(FILE_NAME_ATTR);
                fileName = ObjectUtils.defaultIfNull(fileName, content.getResource().getFileName());
                String headerValue = "attachment; filename=" + fileName;
                bodyBuilder = bodyBuilder.header(CONTENT_DISPOSITION, headerValue);
            }
            return bodyBuilder.body(new InputStreamResource(resource.getInputStream()));
        } catch (IOException e) {
            String message = "Failed to retrieve content with identifier " + id;
            LOGGER.error(message, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }

    @GetMapping("/view/{id}")
    public String view(Model model, @PathVariable("id") String id) throws IOException {
        updateContent(model, id, true);
        return "misc/content";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") String id) throws IOException {
        updateContent(model, id, false);
        return "misc/content";
    }

    private void updateContent(Model model, String id, boolean view) throws IOException {
        requireNonNull(model);
        requireNonNull(id);
        model.addAttribute("id", id);
        model.addAttribute("view", view);
        model.addAttribute("edit", !view);
        Content content = contentService.getContent(id);
        Resource resource = view ? contentService.view(content) : contentService.edit(content);
        model.addAttribute("content", resource.loadAsString());
        String mode = MODES.getOrDefault(content.getMimeType(), "ace/mode/plain_text");
        model.addAttribute("mode", mode);
    }

    private static void registerMode(String mimeType, String mode) {
        MODES.put(mimeType, "ace/mode/" + mode);
    }

    static {
        registerMode("text/html", "html");
        registerMode("text/xml", "xml");
        registerMode("text/java", "java");
        registerMode("text/json", "json");
        registerMode("text/sql", "sql");
        registerMode("text/javascript", "javascript");
        registerMode("application/yaml", "yaml");
        registerMode("application/json", "json");
        registerMode("application/sql", "sql");
    }
}
