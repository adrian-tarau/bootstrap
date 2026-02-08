/*
* The Google Maps Global Variables
 */
window.Google.Maps = window.Google.Maps || {};

/**
 * A callback function called after Google Maps API is initialized.
 *
 * @return {string} the application ID
 */
Google.Maps.initialize = function () {
    Application.fire("google.maps")
}