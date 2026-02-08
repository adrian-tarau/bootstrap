package net.microfalx.bootstrap.feature;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.ClassUtils;
import org.atteo.classindex.ClassIndex;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A service to manage application features.
 */
@Service
@Slf4j
public class FeatureService implements InitializingBean {

    @Autowired(required = false) private FeatureProperties properties = new FeatureProperties();

    private final Map<String, Feature> features = new ConcurrentHashMap<>();
    private final Set<Feature> enabledFeatures = ConcurrentHashMap.newKeySet();

    /**
     * Returns the registered features.
     *
     * @return a non-null instance
     */
    public Set<Feature> getFeatures() {
        return new HashSet<>(features.values());
    }

    /**
     * Registers a new feature
     *
     * @param feature the feature
     */
    public void registerFeature(Feature feature) {
        requireNonNull(feature);
        features.put(toIdentifier(feature.getId()), feature);
    }

    /**
     * Returns a feature by its identifier, if exists.
     *
     * @param id the feature identifier
     * @return the optional feature
     */
    public Optional<Feature> findFeature(String id) {
        requireNotEmpty(id);
        return ofNullable(features.get(toIdentifier(id)));
    }

    /**
     * Returns a feature by its identifier
     *
     * @param id the feature identifier
     * @return the feature
     * @throws FeatureNotFoundException if the feature does not exist
     */
    public Feature getFeature(String id) {
        requireNotEmpty(id);
        return features.get(toIdentifier(id));
    }

    /**
     * Returns the active features.
     *
     * @return a non-null instance
     */
    public Set<Feature> getEnabledFeatures() {
        return Collections.unmodifiableSet(enabledFeatures);
    }

    /**
     * Returns whether a feature is enabled.
     *
     * @param feature the feature
     * @return a {@code true} if enabled, {@code false} otherwise
     */
    public boolean isEnabled(String feature) {
        requireNonNull(feature);
        return enabledFeatures.contains(getFeature(feature));
    }

    /**
     * Returns whether a feature is enabled.
     *
     * @param feature the feature
     * @return a {@code true} if enabled, {@code false} otherwise
     */
    public boolean isEnabled(Feature feature) {
        requireNonNull(feature);
        return enabledFeatures.contains(feature);
    }

    /**
     * Enables (activates) a feature.
     *
     * @param feature the feature
     */
    public void setEnabled(Feature feature, boolean active) {
        requireNonNull(feature);
        if (active) {
            enabledFeatures.add(feature);
        } else {
            enabledFeatures.remove(feature);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadFeatures();
        enableFeatures();
    }

    private void loadFeatures() {
        LOGGER.debug("Loaded features");
        Iterable<Class<? extends Features>> featureClasses = ClassIndex.getSubclasses(Features.class);
        for (Class<? extends Features> featureClass : featureClasses) {
            LOGGER.debug(" - + {}", ClassUtils.getName(featureClass));
            for (Field field : featureClass.getFields()) {
                try {
                    Object value = field.get(null);
                    if (value instanceof Feature feature) {
                        registerFeature(feature);
                    }
                } catch (IllegalAccessException e) {
                    // ignore
                }
            }
        }
        LOGGER.info("Loaded {} features", features.size());
    }

    private void enableFeatures() {

    }
}
