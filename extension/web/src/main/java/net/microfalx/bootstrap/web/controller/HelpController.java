package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.help.*;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.application.Theme;
import net.microfalx.bootstrap.web.util.SecurityUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.UriUtils;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.StringUtils.removeStartSlash;

/**
 * A controller which renders help files.
 */
@RequestMapping("/help")
@Controller
public class HelpController implements AnonymousController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpController.class);

    @Autowired private HelpService helpService;
    @Autowired private ApplicationService applicationService;

    @GetMapping(value = "/image/{*path}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Object> image(@PathVariable("path") String path) {
        checkSecurity();
        try {
            Resource content = HelpUtilities.resolveImage(path);
            if (!content.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(content.getMimeType()))
                    .body(new InputStreamResource(content.getInputStream()));
        } catch (IOException e) {
            String message = "Failed to retrieve image at " + path;
            LOGGER.error(message, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }

    @GetMapping("/about")
    public String about(Model model) {
        return "help/article::#help-about";
    }

    @GetMapping("/view/{*path}")
    public String dialog(Model model, @PathVariable("path") String path,
                         @RequestParam(value = "title") String title,
                         @RequestParam(value = "anchor", required = false) String anchor,
                         @RequestParam(value = "theme", required = false) String theme,
                         @RequestParam(value = "theme_mode", required = false) String themeMode) {
        checkSecurity();
        model.addAttribute("title", title);
        boolean root = UriUtils.isRoot(path);
        path = removeStartSlash(path);
        path = appendThemeToPath(path, theme, themeMode);
        if (isNotEmpty(anchor)) path += "#" + anchor;
        model.addAttribute("path", "/help/article/" + path);
        String dialogClasses = root ? "modal-xl" : "modal-lg";
        model.addAttribute("root", root);
        model.addAttribute("toc", renderToc());
        model.addAttribute("dialogClasses", dialogClasses);
        selectTheme(theme, themeMode);
        return "help/article::#help-article";
    }

    @GetMapping(value = "/article/{*path}")
    public String renderArticle(Model model, @PathVariable("path") String path,
                                @RequestParam(value = "theme", required = false) String theme,
                                @RequestParam(value = "theme_mode", required = false) String themeMode) {
        checkSecurity();
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
        selectTheme(theme, themeMode);
        return "help/layout";
    }

    private void selectTheme(String theme, String themeMode) {
        // first, see if the help asks for a specific theme and mode
        HelpProperties properties = helpService.getProperties();
        if (isNotEmpty(properties.getTheme())) theme = properties.getTheme();
        if (isNotEmpty(properties.getThemeMode())) themeMode = properties.getThemeMode();
        // select the theme based on request and help setup
        Theme activeTheme = applicationService.getCurrentTheme();
        if (isNotEmpty(theme)) activeTheme = applicationService.getTheme(theme);
        if (isNotEmpty(themeMode)) activeTheme = activeTheme.withMode(Theme.Mode.of(themeMode));
        Theme.set(activeTheme);
    }

    private String appendThemeToPath(String path, String theme, String themeMode) {
        if (StringUtils.isEmpty(theme) && StringUtils.isEmpty(themeMode)) return path;
        if (!path.contains("?")) path += "?";
        if (isNotEmpty(theme)) path += "theme=" + theme;
        if (isNotEmpty(themeMode)) path += (path.contains("?") ? "&" : "?") + "theme_mode=" + themeMode;
        return path;
    }

    private String doRender(String path) {
        RenderingOptions options = RenderingOptions.DEFAULT;
        Resource resource;
        try {
            Toc toc = helpService.get(path);
            if (toc.isRoot()) {
                resource = helpService.renderAll(options);
            } else {
                resource = helpService.render(toc, options);
            }
            return resource.loadAsString();
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Failed to render help '" + path + "'", e);
            return "Failed to render help '" + path + "'";
        }
    }

    private String renderToc() {
        try {
            return helpService.renderToc(RenderingOptions.DEFAULT).loadAsString();
        } catch (IOException e) {
            return "Failed to render Table of Contents";
        }
    }

    private void checkSecurity() {
        if (helpService.isSecure() && !SecurityUtils.isAuthenticated()) {
            throw new AccessDeniedException("Help access requires a security context");
        }
    }
}
