/*
* The Google Analytics Global Variables
 */
window.Google.Analytics = window.Google.Analytics || {};

/**
 * A callback function called after Google Maps API is initialized.
 *
 * @return {string} clientId the application ID
 */
Google.Analytics.initialize = function (clientId) {
    Logger.info("Initialize Google Analytics, client id: " + clientId);
    // initialize Google Analytics
    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());
    gtag('config', clientId);
    // let everybody know Analytics is available
    Application.fire("google.analytics");
}