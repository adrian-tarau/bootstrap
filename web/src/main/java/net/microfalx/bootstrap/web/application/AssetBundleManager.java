package net.microfalx.bootstrap.web.application;

import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Manages the asset bundles.
 */
class AssetBundleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetBundleManager.class);

    private final ApplicationService applicationService;
    private final Map<String, AssetBundle> bundles = new ConcurrentHashMap<>();
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

    Resource getAssetBundleContent(String id, Asset.Type type) throws IOException {
        AssetBundle assetBundle = getAssetBundle(id);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputStream stream = new GZIPOutputStream(buffer);
        return MemoryResource.create(buffer.toByteArray(), "assert-bundle-id");
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
        if (!isExpanded(expandedBundles, assetBundle)) {
            expandedBundles.add(new AssetBundleOrder(assetBundle));
        }
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
            AssetBundleOrder _dependency = getAssetBundleOrder(expandedBundles, dependency);
            if (_dependency == null) {
                // the bundle was eliminated, skip
                continue;
            }
            if (_dependency.order >= expandedBundle.order) {
                // since the dependency order is not lower, we need to move it
                changed = true;
                _dependency.order--;
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