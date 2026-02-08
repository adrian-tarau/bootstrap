package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLConnection;
import java.util.Map;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.CollectionUtils.immutableMap;
import static net.microfalx.lang.StringUtils.*;

/**
 * Represents a web media asset (javaScript, stylesheet, font, etc).
 */
public final class Asset implements Identifiable<String>, Nameable, Descriptable, Comparable<Asset> {

    private String id;
    private String name;
    private Type type;
    private String path;
    private Boolean requiresAuthentication;
    private Resource resource;
    private URI uri;
    private boolean async;
    private boolean defer;
    private String onLoad;
    private String feature;
    private Map<String, Object> parameters;

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
     * Creates an asset builder based on an external script.
     *
     * @param type the type
     * @param uri  the URI to the external resource
     * @return a non-null instance
     */
    public static Builder uri(Asset.Type type, String uri) {
        requireNotEmpty(uri);
        return uri(type, URI.create(uri));
    }

    /**
     * Creates an asset builder based on an external script.
     *
     * @param type the type
     * @param uri  the URI to the external resource
     * @return a non-null instance
     */
    public static Builder uri(Asset.Type type, URI uri) {
        return new Builder().type(type).uri(uri);
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

    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return resource != null ? resource.getName() : (uri != null ? uri.getHost() : path);
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
        return uri != null ? uri.getPath() : path;
    }

    /**
     * Returns the URI to access the asset, null if the asset is not external.
     *
     * @return the URI, null if not external
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the external URI with parameters appended.
     *
     * @return the external URI, null if not external
     */
    public URI toExternalUri() {
        if (uri == null) return null;
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(uri);
        if (parameters != null) parameters.forEach(uriBuilder::queryParam);
        return URI.create(uriBuilder.build().toUriString());
    }

    /**
     * Returns the JavaScript function to be called once the asset is loaded.
     *
     * @return the JavaScript function, null if not set
     */
    public String getOnLoad() {
        return onLoad;
    }

    /**
     * Returns whether the asset is hosted externally (i.e. assets with URI).
     *
     * @return {@code true} if external, {@code false} otherwise
     */
    public boolean isExternal() {
        return uri != null;
    }

    /**
     * Returns whether the asset is loaded from memory (i.e. assets with a resource).
     *
     * @return a non-null instance
     */
    public boolean isMemory() {
        return resource != null;
    }

    /**
     * Indicates the asset should be loaded asynchronously (most used in combination with {@link #isDefer()}).
     *
     * @return {@code true} if the asset should be loaded asynchronously
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Returns whether the asset should be deferred (most used in combination with {@link #isAsync()}).
     *
     * @return {@code true} if the asset should be deferred, <code>false</code> otherwise
     */
    public boolean isDefer() {
        return defer;
    }

    /**
     * Returns the feature associated with the asset.
     * <p>
     * If the feature is not active, the asset will not be included.
     *
     * @return the feature id, null if not associated with a feature
     */
    public String getFeature() {
        return feature;
    }

    /**
     * Returns the (query) parameters associated with the asset.
     * <p>
     * Mostly used with external assets.
     *
     * @return a non-null instance
     */
    public Map<String, Object> getParameters() {
        return immutableMap(parameters);
    }

    /**
     * Returns whether this asset will be rendered based on the security context.
     *
     * @return {@code true} if the asset is required only on authenticated pages, {@code false} if the asset is required on
     * pages accessible to anonymous users as well, null if not specified (i.e. the asset is required on all pages)
     */
    public Boolean isRequiresAuthentication() {
        return requiresAuthentication;
    }

    /**
     * Returns the asset order within the asset bundle.
     * <p/>
     * By default, if an asset does not have an order, they will receive an order based on their position in the asset bundle.
     *
     * @return the order of the asset
     */
    public int getOrder() {
        return order;
    }

    /**
     * Returns the last time when the asses was modified.
     *
     * @return millis
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Returns the resource to access the asset's content.
     *
     * @return a non-null instance
     */
    public Resource getResource() {
        if (resource != null) {
            return resource;
        } else {
            String fullPath = getPathFromType();
            if (assetBundle != null && isNotEmpty(assetBundle.getPath())) {
                fullPath += addStartSlash(assetBundle.getPath());
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
    public AssetBundle getAssetBundle() {
        return assetBundle;
    }

    /**
     * Returns the content type (mime type) associated with the asset.
     *
     * @return the content type
     */
    public String getContentType() {
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

    @Override
    public String toString() {
        return new StringJoiner(", ", Asset.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("type=" + type)
                .add("path='" + path + "'")
                .add("requiresAuthentication=" + requiresAuthentication)
                .add("uri=" + uri)
                .add("resource=" + resource)
                .add("order=" + order)
                .add("assetBundle=" + assetBundle.getName())
                .add("lastModified=" + lastModified)
                .toString();
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

        private URI uri;
        private boolean async;
        private boolean defer;
        private String feature;
        private String onLoad;

        private Resource resource;
        private Map<String, Object> parameters;

        private Boolean requiresAuthentication;

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
            this.id = toIdentifier(id);
            return this;
        }

        public Builder type(Type type) {
            requireNonNull(type);
            this.type = type;
            return this;
        }

        public Builder feature(String feature) {
            this.feature = feature;
            return this;
        }

        public Builder resource(Resource resource) {
            requireNonNull(resource);
            this.id = resource.toHash();
            this.resource = resource;
            return this;
        }

        public Builder uri(URI uri) {
            requireNonNull(uri);
            if (isEmpty(id)) id = Hashing.get(uri.toASCIIString());
            this.uri = uri;
            return this;
        }

        public Builder onLoad(String onLoad) {
            this.onLoad = onLoad;
            return this;
        }

        public Builder async(boolean async) {
            this.async = async;
            return this;
        }

        public Builder defer(boolean defer) {
            this.defer = defer;
            return this;
        }

        public Builder parameter(String name, Object value) {
            requireNonNull(name);
            if (parameters == null) parameters = new java.util.LinkedHashMap<>();
            parameters.put(name, value);
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
            if (isEmpty(path) && resource == null && uri == null) {
                throw new IllegalArgumentException("A resource, an URI or a class loader path need to be provided");
            }
            if (resource != null && !ResourceUtils.exists(resource)) {
                throw new IllegalArgumentException("The resource does not exist: " + resource.toURI());
            }
            Asset asset = new Asset();
            asset.id = id;
            asset.type = type;
            asset.order = order;
            asset.feature = feature;
            asset.path = path;
            asset.uri = uri;
            asset.async = async;
            asset.defer = defer;
            asset.onLoad = onLoad;
            asset.resource = resource;
            asset.parameters = parameters;
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
