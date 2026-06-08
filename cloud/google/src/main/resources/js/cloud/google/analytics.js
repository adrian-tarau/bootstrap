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
    this.push('js', new Date());
    const isLocalhost = location.hostname === 'localhost' || location.hostname === '127.0.0.1';
    this.push('config', clientId, {
        user_id: User.getId(),
        debug_mode: isLocalhost
    });
    // let everybody know Analytics is available
    Application.fire("google.analytics");

    // replaces the analytics
    Application.analytics = function (event, options) {
        Google.Analytics.pushEvent(event, options);
    };
}

/**
 * Pushes data to analytics.
 *
 * @param {...*} args the arguments
 */
Google.Analytics.push = function (...args) {
    window.dataLayer = window.dataLayer || [];
    dataLayer.push(arguments);
}

/**
 * Pushes an event to the analytics.
 *
 * @param event the event name
 * @param options the options
 */
Google.Analytics.pushEvent = function (event, options) {
    options = options || {};
    this.push('event', event, options);
    Logger.info("Google Analytics, event: " + event + ", options: " + JSON.stringify(options));
}