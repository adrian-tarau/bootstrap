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
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.self=true] an optional boolean, to calculate the URI to the current page (self)
 * @param {Boolean} [options.params=true] an optional boolean, to include the parameters in the URI
 */
Application.uri = function (params, path, options) {
    options = options || {};
    options.self = (typeof options.self === 'undefined') ? true : options.self;
    options.params = (typeof options.params === 'undefined') ? true : options.params;
    params = self ? Application.query(params) : params;
    let uri = self ? REQUEST_PATH : "";
    if (path) {
        if (path.startsWith("/")) path = path.substring(1);
        uri += "/" + path;
    }
    if (options.params) uri += "?" + $.param(params);
    console.info("Application URI: " + uri);
    return uri;
}

/**
 * Takes a collection of parameters, merges them on top of the current parameters and opens a path under the same
 * path as the current page.
 *
 * @param {Object} params the new parameters
 * @param {String} [path] an optional path to add to the base URI
 */
Application.openSelf = function (params, path) {
    window.location.href = Application.uri(params, path, {self: true});
}

/**
 * Takes a collection of parameters and opens a page at requested path.
 *
 * @param {Object} params the new parameters
 * @param {String} [path] an optional path to add to the base URI
 */
Application.open = function (params, path) {
    window.location.href = Application.uri(params, path, {self: false});
}

/**
 * Reloads the current page.
 */
Application.reload = function () {
    Application.openSelf({});
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters.
 *
 * @param {String} path the path
 * @param {Object} params the new parameters
 * @param {Function} callback the callback to be called with the response
 * @param {Boolean} [self=true] an optional boolean, to calculate the URI to the current page (self)
 */
Application.ajax = function (path, params, callback, self) {
    let requestParams = Application.query(params);
    let uri = Application.uri({}, path, {self: self, params : false});
    console.info("Ajax Request: " + uri);
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

/**
 * Triggers a search.
 *
 * @param {String} query the query passed to the search engine (Apache Lucene syntax)
 */
Application.search = function (query) {
    if ($.isEmptyObject(query)) query = $("#search").val();
    let params = {
        query: encodeURIComponent(query)
    }
    Application.open(params, "/search")
}


