/*
* The Application Server Side Events Global Variables
 */
window.Application.Sse = window.Application.Sse || {};

/**
 * A constant for the end of data marker in Server-Sent Events (SSE).
 * @type {string}
 */
const SSE_END_OF_DATA = "$END_OF_DATA$";

/**
 * Starts a Server-Sent Events (SSE) connection.
 *
 * @param {String} path an optional path to add to the base URI for the SSE connection
 * @param {Function} callback a callback function to receive the events from the server
 * @param {Object} [params] an optional list of parameters
 * @param {Object} [options] an optional object, to control how parameters are calculated *
 */
Application.Sse.start = function (path, callback, params, options) {
    if (!Utils.isFunction(callback)) {
        throw new Error("A callback function is required to handle the events from the server");
    }
    params.applicationId = Application.getId();
    let uri = Application.getUri(path, params, options);
    const events = new EventSource(uri, {});
    events.onmessage = (event) => {
        let data = event.data;
        if (SSE_END_OF_DATA === data) {
            Logger.debug("Received end of data from server");
            events.close();
        } else {
            Logger.debug("Received data from server, data: " + data);
            callback.call(callback, data, event);
        }
    };
    events.onopen = (event) => {
        Logger.debug("The connection has been established to '" + event.target.uri + "'");
    };
    events.onerror = (event) => {
        Logger.warn("Received a failure from '" + event.target.uri + "'");
    };
}

/**
 * Initialize the SSE channel.
 */
Application.Sse.initialize = function () {
    Application.Sse.start("/event/out", function (data, event) {
        let json = JSON.parse(data);
        let name = json.name;
        delete json.name;
        delete json.application;
        Logger.debug("Received SSE event '" + name + "', data: " + data);
        Application.fire(name, json);
    }, {}, {self: false});
}

// initialize application
Application.Sse.initialize();