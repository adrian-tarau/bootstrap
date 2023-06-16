package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Manages the asset bundles.
 */
class AssetBundleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetBundleManager.class);

    private final ApplicationService applicationService;
    private final Map<String, AssetBundle> bundles = new ConcurrentHashMap<>();
    private final Map<String, Theme> themes = new ConcurrentHashMap<>();
    private final Map<String, Resource> bundlesContent = new ConcurrentHashMap<>();
    private final Map<String, Collection<String>> assetBundleDependencies = new ConcurrentHashMap<>();

    AssetBundleManager(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    ApplicationService getApplicationService() {
        return applicationService;
    }

    Application getApplication() {
        return applicationService.getApplication();
    }

    Collection<Theme> getThemes() {
        return Collections.unmodifiableCollection(themes.values());
    }

    Theme getTheme(String idOrName) {
        requireNotEmpty(idOrName);
        Theme theme = themes.get(StringUtils.toIdentifier(idOrName));
        if (theme == null)
            throw new ApplicationException("A theme with identifier or name '" + idOrName + "' is not registered");
        return theme;
    }

    void registerTheme(Theme theme) {
        requireNonNull(theme);
        LOGGER.debug("Register theme {}, name {}", theme.getId(), theme.getName());
        themes.put(theme.getId(), theme);
        themes.put(StringUtils.toIdentifier(theme.getName()), theme);
    }

    void registerAssetBundle(AssetBundle assetBundle) {
        requireNonNull(assetBundle);
        LOGGER.debug("Register asset bundle {}, name {}", assetBundle.getId(), assetBundle.getName());
        bundles.put(assetBundle.getId(), assetBundle);
    }

    AssetBundle getAssetBundle(String id) {
        AssetBundle assetBundle = bundles.get(id);
        if (assetBundle == null)
            throw new ApplicationException("An asset bundle with identifier '" + id + "' is not registered");
        return assetBundle;
    }

    Resource getAssetBundlesContent(Asset.Type type, boolean header, String... ids) throws IOException {
        requireNonNull(type);
        requireNotEmpty(ids);
        String contentId = type.name().toLowerCase() + "/" + StringUtils.join("_", ids);
        Resource resource = bundlesContent.get(contentId);
        if (resource != null) return resource;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputStream stream = new GZIPOutputStream(buffer);
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            int counter = 0;
            for (String id : ids) {
                AssetBundle assetBundle = getAssetBundle(id);
                for (Asset asset : assetBundle.getAssets()) {
                    if (asset.getType() != type) continue;
                    if (header) {
                        writer.append("/*\nAsset: ").append(asset.getName()).append(", path ")
                                .append(asset.getPath()).append("\n*/\n\n");
                    }
                    writer.append(asset.getResource().loadAsString());
                    if (counter < ids.length - 1) {
                        writer.append("\n\n");
                    }
                }
                counter++;
            }
        }
        resource = MemoryResource.create(buffer.toByteArray(), contentId);
        bundlesContent.put(contentId, resource);
        return resource;
    }

    Collection<AssetBundle> getAssetBundles() {
        return Collections.unmodifiableCollection(bundles.values());
    }

    Collection<AssetBundle> expandBundles(Collection<AssetBundle> bundles) {
        List<AssetBundleOrder> expandedBundles = new ArrayList<>();
        for (AssetBundle bundle : bundles) {
            expandBundles(expandedBundles, bundle);
        }
        for (int i = 0; i < 100; i++) {
            boolean orderChanged = false;
            for (AssetBundleOrder expandedBundle : expandedBundles) {
                orderChanged |= shiftOrder(expandedBundles, expandedBundle);
            }
            // if nothing changed, exit, they are in the right order
            if (!orderChanged) break;
        }
        expandedBundles.sort(AssetBundleOrder::compareTo);
        List<AssetBundle> orderedBundles = new ArrayList<>();
        for (AssetBundleOrder expandedBundle : expandedBundles) {
            orderedBundles.add(expandedBundle.assetBundle);
        }
        return orderedBundles;
    }

    Collection<String> getDependenciesForBundle(String id) {
        return assetBundleDependencies.computeIfAbsent(id, s -> new CopyOnWriteArrayList<>());
    }

    void load() {
        AssetBundleLoader loader = new AssetBundleLoader(this);
        loader.load();
    }

    private void expandBundles(Collection<AssetBundleOrder> expandedBundles, AssetBundle assetBundle) {
        if (!isExpanded(expandedBundles, assetBundle)) expandedBundles.add(new AssetBundleOrder(assetBundle));
        Collection<String> dependenciesForBundle = getDependenciesForBundle(assetBundle.getId());
        for (String dependency : dependenciesForBundle) {
            AssetBundle dependencyAssetBundle = applicationService.getAssetBundle(dependency);
            if (!isExpanded(expandedBundles, dependencyAssetBundle)) {
                expandBundles(expandedBundles, dependencyAssetBundle);
            }
        }
    }

    private boolean shiftOrder(Collection<AssetBundleOrder> expandedBundles, AssetBundleOrder expandedBundle) {
        Collection<String> dependenciesForBundle = getDependenciesForBundle(expandedBundle.assetBundle.getId());
        boolean changed = false;
        for (String dependency : dependenciesForBundle) {
            AssetBundleOrder resolvedDependency = getAssetBundleOrder(expandedBundles, dependency);
            // the bundle was eliminated, skip
            if (resolvedDependency == null) continue;
            if (resolvedDependency.order >= expandedBundle.order) {
                // since the dependency order is not lower, we need to move it
                changed = true;
                resolvedDependency.order--;
            }
        }
        return changed;
    }

    private boolean isExpanded(Collection<AssetBundleOrder> expandedBundles, AssetBundle bundle) {
        for (AssetBundleOrder expandedBundle : expandedBundles) {
            if (expandedBundle.assetBundle.equals(bundle)) {
                return true;
            }
        }
        return false;
    }

    private AssetBundleOrder getAssetBundleOrder(Collection<AssetBundleOrder> expandedBundles, String id) {
        for (AssetBundleOrder expandedBundle : expandedBundles) {
            if (id.equals(expandedBundle.assetBundle.getId())) {
                return expandedBundle;
            }
        }
        return null;
    }

    static class AssetBundleOrder implements Comparable<AssetBundleOrder> {

        private final AssetBundle assetBundle;
        private int order;

        AssetBundleOrder(AssetBundle assetBundle) {
            this.assetBundle = assetBundle;
            this.order = assetBundle.getOrder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AssetBundleOrder that = (AssetBundleOrder) o;

            return assetBundle.equals(that.assetBundle);
        }

        @Override
        public int hashCode() {
            return assetBundle.hashCode();
        }

        @Override
        public int compareTo(AssetBundleOrder o) {
            return Integer.compare(order, o.order);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("AssetBundleOrder{");
            sb.append("assetBundle=").append(assetBundle);
            sb.append(", order=").append(order);
            sb.append('}');
            return sb.toString();
        }
    }
}
