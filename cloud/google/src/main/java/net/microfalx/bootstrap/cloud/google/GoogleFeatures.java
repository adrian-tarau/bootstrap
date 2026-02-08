package net.microfalx.bootstrap.cloud.google;

import net.microfalx.bootstrap.feature.Feature;
import net.microfalx.bootstrap.feature.Features;

/**
 * Google-related features.
 */
public class GoogleFeatures implements Features {

    /**
     * The feature identifier for Google Maps API.
     */
    public static final String MAP = "google.maps";

    /**
     * The feature identifier for Google Identity Services API.
     */
    public static final String GIS = "google.gis";

    /**
     * Google Map feature.
     */
    public static final Feature MAP_FEATURE = Feature.create(MAP, "Google Maps");

    /**
     * Google Identity Services feature.
     */
    public static final Feature GIS_FEATURE = Feature.create(GIS, "Google Identity Services");
}
