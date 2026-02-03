package net.microfalx.bootstrap.web.application;

import java.util.Collection;

/**
 * A listener used to update asset bundles with dynamic content.
 */
public interface AssetBundleListener {

    /**
     * Returns whether the asset bundle is supported by this listener.
     *
     * @param assetBundle the asset bundle
     * @return @{code true} if supported, {@code false} otherwise
     */
    boolean supports(AssetBundle assetBundle);

    /**
     * Updates the asset bundle.
     *
     * @param assetBundle the asset bundle to update
     * @param assets      the collection with asserts to be added
     */
    void update(AssetBundle assetBundle, Collection<Asset> assets);
}
