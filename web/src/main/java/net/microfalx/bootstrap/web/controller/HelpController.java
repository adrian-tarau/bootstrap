package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.help.HelpNotFoundException;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.StringWriter;

import static net.microfalx.lang.StringUtils.removeStartSlash;

/**
 * A controller which renders help files.
 */
@RequestMapping("/help")
@Controller
public class HelpController extends PageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpController.class);

    @Autowired
    private HelpService helpService;

    @GetMapping(value = "/image/{*path}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Object> image(@PathVariable("path") String path) {
        String resourcePath = helpService.resolveImage(path);
        try {
            Resource content = ClassPathResource.file(resourcePath);
            if (!content.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(content.getMimeType()))
                    .body(new InputStreamResource(content.getInputStream()));
        } catch (IOException e) {
            String message = "Failed to retrieve image at " + path;
            LOGGER.error(message, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }

    @GetMapping("/view/{*path}")
    public String dialog(Model model, @PathVariable("path") String path, @RequestParam(value = "title") String title) {
        model.addAttribute("title", title);
        model.addAttribute("path", "/help/article/" + removeStartSlash(path));
        return "help/article::#help-article";
    }

    @GetMapping(value = "/article/{*path}")
    public String renderArticle(Model model, @PathVariable("path") String path) {
        String content;
        try {
            content = doRender(path);
        } catch (HelpNotFoundException e) {
            content = "A help with path '" + path + "' is not available";
        } catch (Exception e) {
            content = "Failed to render help '" + path + "'";
            LOGGER.error(content, e);
        }
        model.addAttribute("content", content);
        return "help/layout";
    }

    private String doRender(String path) throws IOException {
        StringWriter writer = new StringWriter();
        helpService.render(path, writer);
        writer.close();
        return writer.toString();
    }
}
