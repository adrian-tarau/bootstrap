package net.microfalx.bootstrap.web.application;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Holds information about a theme.
 */
@Getter
@ToString
public final class Theme implements Identifiable<String>, Nameable, Cloneable {

    public static final String SYSTEM = "adminlte";
    public static final String DEFAULT = "adminlte";

    private final String id;
    private final String name;
    /**
     * The ode for this theme
     */
    private Mode mode = Mode.AUTO;
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

    /**
     * Creates a copy of the theme, with a different mode.
     *
     * @param mode the new mode
     * @return a new instance
     */
    public Theme withMode(Mode mode) {
        requireNonNull(mode);
        Theme copy = copy();
        copy.mode = mode;
        return copy;
    }

    /**
     * Returns the asset bundles associated with the theme.
     *
     * @return a non-null instance
     */
    public Collection<AssetBundle> getAssetBundles() {
        return unmodifiableCollection(assetBundles);
    }

    void addAssetBundle(AssetBundle assetBundle) {
        requireNonNull(assetBundle);
        this.assetBundles.add(assetBundle);
    }

    private Theme copy() {
        try {
            return (Theme) clone();
        } catch (CloneNotSupportedException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    /**
     * An enum for the modes of a theme.
     */
    public enum Mode {

        /**
         * Use the light mode.
         */
        LIGHT,

        /**
         * Use the dark mode.
         */
        DARK,

        /**
         * Use the system mode
         */
        AUTO;

        public static Mode of(String mode) {
            return EnumUtils.fromName(Mode.class, mode, AUTO);
        }
    }

    public static class Builder {

        private String id;
        private final String name;
        private final Collection<AssetBundle> assetBundles = new ArrayList<>();
        private Mode mode = Mode.AUTO;

        Builder(String name) {
            this.name = name;
            this.id = toIdentifier(name);
        }

        public Builder id(String id) {
            requireNotEmpty(id);
            this.id = toIdentifier(id);
            return this;
        }

        public Builder mode(Mode mode) {
            requireNotEmpty(mode);
            this.mode = mode;
            return this;
        }

        public Builder assetBundle(AssetBundle assetBundle) {
            requireNonNull(assetBundle);
            this.assetBundles.add(assetBundle);
            return this;
        }

        public Theme build() {
            Theme theme = new Theme(id, name);
            theme.mode = mode;
            theme.assetBundles.addAll(assetBundles);
            return theme;
        }


    }
}
