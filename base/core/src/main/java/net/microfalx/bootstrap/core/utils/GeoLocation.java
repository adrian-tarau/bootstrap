package net.microfalx.bootstrap.core.utils;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

/**
 * An interface for objects which carries geographical location information.
 */
public interface GeoLocation extends Identifiable<String>, Nameable, Descriptable {

    /**
     * Returns the country name.
     *
     * @return a non-null instance
     */
    String getCountry();

    /**
     * Returns the country code.
     *
     * @return a non-null instance
     */
    String getCountryCode();

    /**
     * Returns the region name.
     *
     * @return a non-null instance
     */
    String getRegion();

    /**
     * Returns the region code.
     *
     * @return a non-null instance
     */
    String getRegionCode();

    /**
     * Returns the city name.
     *
     * @return a non-null instance
     */
    String getCity();

    /**
     * Returns the latitude.
     *
     * @return the latitude, can be null
     */
    Double getLatitude();

    /**
     * Returns the longitude.
     *
     * @return the longitude, can be null
     */
    Double getLongitude();
}
