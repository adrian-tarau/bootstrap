package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;

import java.net.URLConnection;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.*;

/**
 * Represents a web media asset (javaScript, stylesheet, font, etc).
 */
public final class Asset implements Identifiable<String>, Nameable, Descriptable, Comparable<Asset> {

    private String id;
    private String name;
    private Type type;
    private String path;
    private boolean requiresAuthentication;
    private Resource resource;

    int order = Integer.MIN_VALUE;
    AssetBundle assetBundle;
    long lastModified;

    /**
     * Creates an asset builder based on a class path resource.
     *
     * @param type the type
     * @param path the path
     * @return a non-null instance
     */
    public static Builder file(Asset.Type type, String path) {
        return new Builder().type(type).path(path);
    }

    /**
     * Creates an asset builder based on a resource.
     *
     * @param type     the type
     * @param resource the path
     * @return a non-null instance
     */
    public static Builder resource(Asset.Type type, Resource resource) {
        return new Builder().type(type).resource(resource);
    }

    public final String getId() {
        return id;
    }

    @Override
    public String getName() {
        return resource != null ? resource.getName() : path;
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(id).append(", type ").append(type.name());
        return builder.toString();
    }

    /**
     * Returns the resource type.
     *
     * @return a non-null enum
     */
    public final Type getType() {
        return type;
    }

    /**
     * Returns the resource path.
     * <p/>
     * The complete resource path is calculated based on the resource type, resource group
     * path & version and the resource path.
     *
     * @return the path, null if not loaded from class loader.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns whether this asses is required only on authenticated pages.
     *
     * @return <code>true</code> if the asset requires authenticated pages
     */
    public final boolean isRequiresAuthentication() {
        return requiresAuthentication;
    }

    /**
     * Returns the asset order within the asset bundle.
     * <p/>
     * By default, if an asset does not have an order, they will receive an order based on their position in the asset bundle.
     *
     * @return the order of the asset
     */
    public final int getOrder() {
        return order;
    }

    /**
     * Returns the last time when the asses was modifled.
     *
     * @return millis
     */
    public final long getLastModified() {
        return lastModified;
    }

    /**
     * Returns the resource to access the asset's content.
     *
     * @return a non-null instance
     */
    public final Resource getResource() {
        if (resource != null) {
            return resource;
        } else {
            String fullPath = getPathFromType();
            if (assetBundle != null && isNotEmpty(assetBundle.getPath())) {
                fullPath += StringUtils.addStartSlash(assetBundle.getPath());
            }
            fullPath += addStartSlash(path);
            return ClassPathResource.file(fullPath);
        }
    }

    /**
     * Returns the asset bundle which owns this asset.
     *
     * @return a non-null instance
     */
    public final AssetBundle getAssetBundle() {
        return assetBundle;
    }

    /**
     * Returns the content type (mime type) associated with the asset.
     *
     * @return the content type
     */
    public final String getContentType() {
        return switch (type) {
            case STYLE_SHEET -> "text/css";
            case JAVA_SCRIPT -> "text/javascript";
            case IMAGE -> URLConnection.guessContentTypeFromName(getName());
            default -> "application/octet-stream";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Asset asset = (Asset) o;

        if (!id.equals(asset.id)) return false;
        return type == asset.type;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public int compareTo(Asset asset) {
        if (asset == null) return 0;
        return Integer.compare(order, asset.order);
    }

    private String getPathFromType() {
        return switch (type) {
            case JAVA_SCRIPT -> "js";
            case STYLE_SHEET -> "css";
            case IMAGE -> "image";
            case FONT -> "font";
        };
    }

    public static class Builder {

        private String id;
        private Type type;
        private String path;
        private int order;

        private Resource resource;

        private boolean requiresAuthentication;

        Builder() {
        }

        public Builder path(String path) {
            requireNotEmpty(path);
            id(path);
            this.path = path;
            return this;
        }

        public Builder id(String id) {
            requireNonNull(id);
            this.id = StringUtils.toIdentifier(id);
            return this;
        }

        public Builder type(Type type) {
            requireNonNull(type);
            this.type = type;
            return this;
        }

        public Builder resource(Resource resource) {
            requireNonNull(resource);
            this.resource = resource;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder requiresAuthentication(boolean requiresAuthentication) {
            this.requiresAuthentication = requiresAuthentication;

            return this;
        }

        public Asset build() {
            requireNonNull(id);
            requireNonNull(type);
            if (isEmpty(path) && resource == null)
                throw new IllegalArgumentException("A resource or a class loader path need to be provided");

            Asset asset = new Asset();
            asset.id = id;
            asset.type = type;
            asset.order = order;
            asset.path = path;
            asset.requiresAuthentication = requiresAuthentication;
            return asset;
        }
    }

    /**
     * An enum used to identify types of resources
     */
    public enum Type {

        /**
         * A JavaScript asset
         */
        JAVA_SCRIPT,

        /**
         * A stylesheet asset
         */
        STYLE_SHEET,

        /**
         * An image asset
         */
        IMAGE,

        /**
         * A font asset
         */
        FONT
    }
}
