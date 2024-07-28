package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Holds information about a theme.
 */
public final class Theme implements Identifiable<String>, Nameable {

    private final String id;
    private final String name;
    private final Collection<AssetBundle> assetBundles = new ArrayList<>();

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public Theme(String name) {
        this(toIdentifier(name), name);
    }

    private Theme(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public Collection<AssetBundle> getAssetBundles() {
        return Collections.unmodifiableCollection(assetBundles);
    }

    void addAssetBundle(AssetBundle assetBundle) {
        requireNonNull(assetBundle);
        this.assetBundles.add(assetBundle);
    }

    @Override
    public String toString() {
        return "Theme{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", assetBundles=" + assetBundles +
                '}';
    }

    public static class Builder {

        private String id;
        private final String name;
        private final Collection<AssetBundle> assetBundles = new ArrayList<>();

        Builder(String name) {
            this.name = name;
            this.id = toIdentifier(name);
        }

        public Builder id(String id) {
            requireNotEmpty(id);
            this.id = toIdentifier(id);
            return this;
        }

        public Builder assetBundle(AssetBundle assetBundle) {
            requireNonNull(assetBundle);
            this.assetBundles.add(assetBundle);
            return this;
        }

        public Theme build() {
            Theme theme = new Theme(id, name);
            theme.assetBundles.addAll(assetBundles);
            return theme;
        }


    }
}
