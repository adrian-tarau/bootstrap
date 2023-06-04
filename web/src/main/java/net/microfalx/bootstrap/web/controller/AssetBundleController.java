package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.web.application.ApplicationException;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.application.Asset;
import net.microfalx.bootstrap.web.application.AssetBundle;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping(value = "/asset")
public class AssetBundleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetBundleController.class);

    @Autowired
    private ApplicationService applicationService;

    @GetMapping(value = "/css/{id}", produces = "text/css")
    public ResponseEntity<Object> stylesheet(@PathVariable("id") String id) {
        return get(id, Asset.Type.STYLE_SHEET);
    }

    @GetMapping(value = "/js/{id}", produces = "text/css")
    public ResponseEntity<Object> javaScript(@PathVariable("id") String id) {
        return get(id, Asset.Type.JAVA_SCRIPT);
    }

    private ResponseEntity<Object> get(String id, Asset.Type type) {
        try {
            AssetBundle assetBundle = applicationService.getAssetBundle(id);
            Resource content = applicationService.getAssetBundleContent(id, type);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(assetBundle.getContentType())).body(content.getInputStream());
        } catch (ApplicationException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            String message = "Failed to retrieve asset bundle content " + id;
            LOGGER.error(message, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }
}
