package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.web.application.ApplicationException;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.application.Asset;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.Duration;

import static net.microfalx.bootstrap.web.controller.AssetUtils.getContentType;
import static net.microfalx.lang.StringUtils.removeStartSlash;

@Controller
@RequestMapping(value = "/asset")
public final class AssetBundleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetBundleController.class);

    @Autowired
    private ApplicationService applicationService;

    @GetMapping(value = "/css/{id}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Object> stylesheet(@PathVariable("id") String id) {
        return getBundle(id, Asset.Type.STYLE_SHEET);
    }

    @GetMapping(value = "/js/{id}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Object> javaScript(@PathVariable("id") String id) {
        return getBundle(id, Asset.Type.JAVA_SCRIPT);
    }

    @GetMapping(value = "/font/{*path}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Object> font(@PathVariable("path") String path) {
        return AssetUtils.getResource(removeStartSlash(path), Asset.Type.FONT);
    }

    @GetMapping(value = "/image/{*path}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Object> image(@PathVariable("path") String path) {
        return AssetUtils.getResource(removeStartSlash(path), Asset.Type.IMAGE);
    }

    private ResponseEntity<Object> getBundle(String id, Asset.Type type) {
        try {
            Resource content = applicationService.getAssetBundleContent(type, id);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(getContentType(type)))
                    .eTag(content.toHash()).cacheControl(CacheControl.maxAge(Duration.ofMinutes(15)))
                    .body(new InputStreamResource(content.getInputStream()));
        } catch (ApplicationException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            String message = "Failed to retrieve asset bundle " + id;
            LOGGER.error(message, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }
}
