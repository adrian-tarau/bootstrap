/*
* The Application Global Variables
 */
window.Application = window.Application || {};
window.REQUEST_PATH = window.REQUEST_PATH || "/";
window.REQUEST_QUERY = window.REQUEST_QUERY || null;

/**
 * Takes a collection of parameters and creates an object with all query parameters.
 *
 * @param {Object} params the new parameters
 */
Application.query = function (params) {
    let requestParams = $.extend({}, REQUEST_QUERY);
    requestParams = $.extend(requestParams, params);
    return requestParams;
}

/**
 * Takes a collection of parameters and creates a URI (path + query parameters).
 *
 * @param {Object} params the new parameters
 * @param {String} [path] an optional path to add to the base URI
 */
Application.uri = function (params, path) {
    let requestParams = Application.query(params);
    let uri = REQUEST_PATH;
    if (path) {
        if (path.startsWith("/")) path.substring(1);
        uri += "/" + path;
    }
    uri += "?" + $.param(requestParams);
    console.info("Application URI: " + uri);
    return uri;
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters.
 *
 * @param {Object} params the new parameters
 * @param {String} [path] an optional path to add to the base URI
 */
Application.open = function (params, path) {
    window.location.href = Application.uri(params, path);
}

/**
 * Reloads the application set page.
 */
Application.reload = function () {
    Application.open({});
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters.
 *
 * @param {String} path the path
 * @param {Object} params the new parameters
 * @param {Function} callback the callback to be called with the response
 */
Application.ajax = function (path, params, callback) {
    let requestParams = Application.query(params);
    if (path.startsWith("/")) path.substring(1);
    let uri = REQUEST_PATH + "/" + path;
    console.info("Ajax data set " + uri);
    $.get({
        data: requestParams,
        url: uri,
        success: function (output, status, xhr) {
            callback.apply(this, [output, status, xhr]);
        }
    });
}

/**
 * Returns the query parameter with a given name.
 *
 * @param {String} name the parameter name
 */
Application.getQueryParam = function (name) {
    let url_string = location.href;
    let url = new URL(url_string);
    let val = url.searchParams.get(arguments[0]);
    return val;
}


