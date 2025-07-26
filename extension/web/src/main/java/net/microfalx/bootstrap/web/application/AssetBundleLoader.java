package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.StringUtils;
import net.microfalx.lang.XmlUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.XmlUtils.*;

/**
 * Loads asset bundles from <code>asset.xml</code> descriptors.
 */
final class AssetBundleLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetBundleLoader.class);

    private final AssetBundleManager assetBundleManager;
    private static final Map<String, Asset.Type> resourceTypeMapping = new HashMap<>();

    AssetBundleLoader(AssetBundleManager assetBundleManager) {
        this.assetBundleManager = assetBundleManager;
    }

    void load() {
        LOGGER.debug("Discover assets from web descriptors");
        Collection<URL> webDescriptors = null;
        try {
            webDescriptors = ApplicationUtils.getAssetDescriptors();
            for (URL webDescriptor : webDescriptors) {
                try {
                    loadResources(webDescriptor);
                } catch (Exception e) {
                    LOGGER.error("Failed to load web resources from asset descriptors: " + webDescriptor.toExternalForm(), e);
                }
            }

            for (URL webDescriptor : webDescriptors) {
                try {
                    loadResourceUpdates(webDescriptor);
                } catch (Exception e) {
                    LOGGER.error("Failed to load web resources from asset descriptors: " + webDescriptor.toExternalForm(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to discover web descriptors", e);
        }
        LOGGER.info("Discovered {} asset bundles from web descriptors", assetBundleManager.getAssetBundles().size());
    }

    private void loadResources(URL webDescriptor) throws IOException {
        LOGGER.debug("Load resources from " + webDescriptor.toExternalForm());
        Document document = loadDocument(webDescriptor.openStream());
        Element rootElement = document.getRootElement();
        loadAssetBundles(rootElement);
    }

    private Theme getTheme(String name) {
        Theme theme;
        try {
            theme = assetBundleManager.getTheme(name);
        } catch (ApplicationException e) {
            theme = Theme.builder(name).build();
            assetBundleManager.registerTheme(theme);
        }
        return theme;
    }

    private void loadAssetBundles(Element root) {
        List<Element> assetBundleElements = root.elements("asset-bundle");
        for (Element assetBundleElement : assetBundleElements) {
            AssetBundle.Builder assetBundleBuilder = new AssetBundle.Builder(getRequiredAttribute(assetBundleElement, "id"));
            assetBundleBuilder.name(getRequiredAttribute(assetBundleElement, "name"));
            assetBundleBuilder.path(getAttribute(assetBundleElement, "path"));
            assetBundleBuilder.order(getAttribute(assetBundleElement, "order", 1000));
            assetBundleBuilder.inline(getAttribute(assetBundleElement, "inline", false));
            assetBundleBuilder.theme(getAttribute(assetBundleElement, "theme", (String) null));
            assetBundleBuilder.description(getAttribute(assetBundleElement, "description"));
            String version = getAttribute(assetBundleElement, "version");
            assetBundleBuilder.version(isNotEmpty(version) ? version : assetBundleManager.getApplication().getVersion());
            loadAssets(assetBundleElement, assetBundleBuilder);
            AssetBundle assetBundle = assetBundleBuilder.build();
            loadAssetBundleDependencies(assetBundle, assetBundleElement);
            assetBundleManager.registerAssetBundle(assetBundle);
            String themeName = getAttribute(assetBundleElement, "theme");
            if (StringUtils.isNotEmpty(themeName)) {
                Theme theme = getTheme(themeName);
                theme.addAssetBundle(assetBundle);
                assetBundleManager.registerTheme(theme);
            }
        }
    }

    private void loadResourceUpdates(URL webDescriptor) throws IOException {
        LOGGER.debug("Load resources updates from " + webDescriptor.toExternalForm());
        Document document = loadDocument(webDescriptor.openStream());
        Element rootElement = document.getRootElement();
        loadAssetBundleUpdates(rootElement);
    }

    private void loadAssetBundleUpdates(Element root) {
        List<Element> assetBundleElements = root.elements("update-asset-bundle");
        for (Element assetBundleElement : assetBundleElements) {
            String assetBundleId = getRequiredAttribute(assetBundleElement, "id");
            AssetBundle assetBundle = assetBundleManager.getAssetBundle(assetBundleId);
            LOGGER.debug("Update asset bundle '" + assetBundle.getId() + "'" +
                    (assetBundle.getPath() != null ? ", path '" + assetBundle.getPath() + "'" : "")
                    + ", version " + assetBundle.getVersion());
            loadAssetBundleDependencies(assetBundle, assetBundleElement);
            AssetBundle.Builder tmpAssetBundleBuilder = new AssetBundle.Builder(assetBundleId);
            loadAssets(assetBundleElement, tmpAssetBundleBuilder);
            AssetBundle tmpAssetBundle = tmpAssetBundleBuilder.build();
            for (Asset asset : tmpAssetBundle.getAssets()) {
                LOGGER.debug(" - " + asset.getDescription());
                assetBundle.addAsset(asset);
            }
        }
    }

    private void loadAssetBundleDependencies(AssetBundle assetBundle, Element assetBundleElement) {
        List<Element> dependsOnElements = assetBundleElement.elements("depends-on-asset-bundle");
        for (Element dependsOnElement : dependsOnElements) {
            assetBundleManager.getDependenciesForBundle(assetBundle.getId()).add(XmlUtils.getElementText(dependsOnElement));
        }
    }

    private void loadAssets(Element resourceGroupElement, AssetBundle.Builder resourceGroupBuilder) {
        List<Element> assetElements = resourceGroupElement.elements("asset");
        for (Element assetElement : assetElements) {
            Asset.Type type = resourceTypeMapping.get(getRequiredAttribute(assetElement, "type").toLowerCase());
            String path = getRequiredAttribute(assetElement, "path");
            Asset.Builder resourceBuilder = Asset.file(type, path);
            resourceBuilder.order(getAttribute(assetElement, "order", Integer.MIN_VALUE));
            resourceGroupBuilder.asset(resourceBuilder.build());
        }
    }

    static {
        resourceTypeMapping.put("script", Asset.Type.JAVA_SCRIPT);
        resourceTypeMapping.put("stylesheet", Asset.Type.STYLE_SHEET);
        resourceTypeMapping.put("font", Asset.Type.FONT);
    }
}
