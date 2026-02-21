package net.microfalx.bootstrap.feature;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.StringUtils;
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
        Feature feature = features.get(toIdentifier(id));
        if (feature == null) {
            throw new FeatureNotFoundException("A feature with identifier '" + id + "' does not exist");
        }
        return feature;
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
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setEnabled(Feature feature, boolean enabled) {
        requireNonNull(feature);
        if (enabled) {
            enabledFeatures.add(feature);
        } else {
            enabledFeatures.remove(feature);
        }
        LOGGER.info("Feature '{}' is {}", feature.getName() + " (" + feature.getId() + ")", enabled ? "enabled" : "disabled");
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
        Map<String, Boolean> toBeActivated = properties.getActivation();
        if (toBeActivated.isEmpty()) return;
        LOGGER.debug("Activate features: {}", toBeActivated);
        for (Map.Entry<String, Boolean> entry : toBeActivated.entrySet()) {
            String featureId = entry.getKey();
            if (findFeature(featureId).isEmpty()) {
                LOGGER.debug("Feature '{}' does not exist, registering it", featureId);
                registerFeature(Feature.create(featureId, StringUtils.capitalize(featureId)));
            }
            boolean active = entry.getValue();
            try {
                Feature feature = getFeature(featureId);
                setEnabled(feature, active);
            } catch (FeatureNotFoundException e) {
                LOGGER.error("Cannot activate feature '{}', it does not exist", featureId);
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to activate feature '{}'", featureId);
            }

        }
    }
}
