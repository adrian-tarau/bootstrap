package net.microfalx.bootstrap.cloud.google;

import net.microfalx.bootstrap.feature.Feature;
import net.microfalx.bootstrap.feature.Features;

/**
 * Google-related features.
 */
public class GoogleFeatures implements Features {

    public static final String MAP = "google.maps";

    /**
     * Google Map feature.
     */
    public static final Feature MAP_FEATURE = Feature.create(MAP, "Google Maps");
}
