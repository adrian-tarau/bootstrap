package net.microfalx.bootstrap.feature;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A context holding the feature available for the current thread for controllers.
 */
public class FeatureContext {

    static final ThreadLocal<FeatureContext> featureContext = ThreadLocal.withInitial(FeatureContext::new);

    private final Set<Feature> features = new HashSet<>();
    private final Set<String> featuresIds = new HashSet<>();

    /**
     * Returns the features available in the current context.
     *
     * @return a non-null instance
     */
    public static FeatureContext get() {
        return featureContext.get();
    }

    static void reset() {
        featureContext.remove();
    }

    /**
     * Returns whether the feature is enabled.
     *
     * @param feature the feature
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isEnabled(Feature feature) {
        return features.contains(feature);
    }

    /**
     * Returns whether the feature is enabled.
     *
     * @param feature the feature
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isEnabled(String feature) {
        requireNotEmpty(feature);
        return featuresIds.contains(feature);
    }

    /**
     * Returns the active features.
     *
     * @return a non-null instance
     */
    public Set<Feature> getFeatures() {
        return unmodifiableSet(features);
    }

    void enable(Feature feature) {
        features.add(feature);
        featuresIds.add(feature.getId());
    }
}
