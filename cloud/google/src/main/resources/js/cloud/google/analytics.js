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
    const isLocalhost = location.hostname === 'localhost' || location.hostname === '127.0.0.1';
    gtag('config', clientId, {
        debug_mode: isLocalhost
    });
    // let everybody know Analytics is available
    Application.fire("google.analytics");
}