/*
* The Google Identity Services Global Variables
 */
window.Google.Gis = window.Google.Gis || {};

/**
 * An initialization function called after the API was loaded.
 *
 * @return {Object} the JWT response object containing the ID token
 */
Google.Gis.initialize = function (clientId) {
    Logger.info("Initialize Google Identity Services, client id: " + clientId);
    // first, initialize the Google Identity Services API
    google.accounts.id.initialize({
        client_id: clientId,
        callback: Google.Gis.login,
        ux_mode: "popup",
        debug: true
    });
    // second, intercept the login button and trigger the Google Identity Services API
    let button = $("#login_google");
    button.removeAttr('href');
    button.on('click', function (event) {
        event.preventDefault();
        google.accounts.id.prompt();
    });
}

/**
 * A callback function called after Google Maps API is initialized.
 *
 * @return {Object} the JWT response object containing the ID token
 */
Google.Gis.login = function (response) {
    Logger.info("Google Identity Services response: \n" + JSON.stringify(response));
    let params = {
        idToken: response.credential
    }
    Application.post("/login/google/token", params, function (response) {
        Application.home();
    }, {
        self: false
    });
}