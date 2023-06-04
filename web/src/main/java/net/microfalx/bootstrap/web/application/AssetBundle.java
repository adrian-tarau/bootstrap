package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.microfalx.bootstrap.web.application.ApplicationUtils.NO_VERSION;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;

/**
 * Represents a group of  web resources.
 */
public class AssetBundle implements Identifiable<String>, Nameable, Descriptable {

    private String id;
    private String version;
    private String name;
    private String description;
    private String path;
    private int order = Integer.MIN_VALUE;
    boolean requiresAuthentication;

    private final List<Asset> assets = new CopyOnWriteArrayList<>();

    /**
     * Returns the group identifier.
     *
     * @return a non-null string
     */
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return defaultIfEmpty(name, id);
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Returns the version associated with this resource group.
     *
     * @return a non-null instance
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns an additional path to be added to the resource final path.
     * <p/>
     * Several resources can be grouped together into a directories
     *
     * @return a path or null if resources are not grouped into a directory
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the order of inclusion.
     * <p/>
     * Asset bundles are order by scope and order
     *
     * @return an inclusion order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Returns the resources part of this group.
     * <p>
     * The assest are order based on their {@link Asset#getOrder()}.
     *
     * @return a non-null collection
     */
    public Collection<Asset> getAssets() {
        return Collections.unmodifiableCollection(assets);
    }

    /**
     * Returns the content type of this asset bundle.
     *
     * @return a non-null instance
     */
    public String getContentType() {
        if (assets.isEmpty()) return "application/octet-stream";
        return assets.iterator().next().getContentType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetBundle that = (AssetBundle) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Registers a new asset as is at the end of the list.
     *
     * @param asset the asset
     */
    void addAsset(Asset asset) {
        requireNonNull(asset);
        if (asset.order == Integer.MIN_VALUE) asset.order = this.assets.size();
        if (!this.assets.contains(asset)) this.assets.add(asset);
        asset.assetBundle = this;
    }

    /**
     * Registers a new asset as is as first item.
     *
     * @param asset the asset
     */
    void insertAsset(Asset asset) {
        requireNonNull(asset);
        if (asset.order == Integer.MIN_VALUE) asset.order = this.assets.size();
        if (!this.assets.contains(asset)) this.assets.add(0, asset);
        asset.assetBundle = this;
    }

    public static class Builder {

        private final String id;
        private String version = NO_VERSION;
        private String name;
        private String description;
        private String path;
        private int order;
        private boolean requiresAuthentication;
        private final List<Asset> assets = new ArrayList<>();

        public Builder(String id) {
            requireNonNull(id);
            this.id = StringUtils.toIdentifier(id);
        }

        public Builder name(String name) {
            requireNonNull(name);
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder version(String version) {
            requireNonNull(version);
            this.version = version;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder asset(Asset asset) {
            requireNonNull(asset);

            // if order is not provisioned, give one based the current position in the list
            if (asset.order == Integer.MIN_VALUE) asset.order = this.assets.size();
            this.assets.add(asset);
            return this;
        }

        public Builder requiresAuthentication(boolean requiresAuthentication) {
            this.requiresAuthentication = requiresAuthentication;
            return this;
        }

        public AssetBundle build() {
            AssetBundle assetBundle = new AssetBundle();
            assetBundle.id = id;
            assetBundle.name = name;
            assetBundle.description = description;
            assetBundle.version = version;
            assetBundle.path = path;
            assetBundle.requiresAuthentication = requiresAuthentication;
            assetBundle.order = order;
            for (Asset asset : assets) {
                asset.assetBundle = assetBundle;
                assetBundle.assets.add(asset);
            }
            assetBundle.assets.sort(Comparator.comparingInt(Asset::getOrder));
            return assetBundle;
        }
    }


}
