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
Application.getQuery = function (params) {
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
Application.getUri = function (params, path, options) {
    options = options || {};
    options.self = (typeof options.self === 'undefined') ? true : options.self;
    options.params = (typeof options.params === 'undefined') ? true : options.params;
    params = options.self ? this.getQuery(params) : params;
    let uri = options.self ? REQUEST_PATH : "/";
    if (path) {
        if (!uri.endsWith("/")) uri += "/";
        if (path.startsWith("/")) path = path.substring(1);
        uri += path;
    }
    if (options.params) uri += "?" + $.param(params);
    Logger.debug("Resolve URI for path '" + path + "', params '" + Utils.toString(params) + "', uri '" + uri + "'");
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
    let uri = this.getUri(params, path, {self: true});
    Logger.info("Open '" + uri + "'");
    window.location.href = uri;
}

/**
 * Takes a collection of parameters and opens a page at requested path, outside the current page.
 *
 * @param {Object} params the new parameters
 * @param {String} [path] an optional path to add to the base URI
 */
Application.open = function (params, path) {
    let uri = this.getUri(params, path, {self: false});
    Logger.info("Open '" + uri + "'");
    window.location.href = uri;
}

/**
 * Reloads the current page.
 */
Application.reload = function () {
    this.openSelf({});
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
    let requestParams = this.getQuery(params);
    let uri = this.getUri({}, path, {self: self, params: false});
    Logger.info("Ajax Request: " + uri);
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
    return url.searchParams.get(arguments[0]);
}

/**
 * Closes all open popups.
 */
Application.closePopups = function () {
    this.getPopups().forEach(function (popup) {
        popup.hide();
    });
    this.popups = [];
}

/**
 * Registers a popup, which is used to validate if the user clicks outside the popup.
 * @param {Object} element a
 */
Application.registerPopup = function (element) {
    this.getPopups().push(element);
}

/**
 * Returns the registers popups.
 *
 * @return {Object[]} the popups.
 */
Application.getPopups = function () {
    this.popups = this.popups || [];
    return this.popups;
}

/**
 * Closes last dialog.
 */
Application.closeModal = function () {
    let modal = this.getModals().pop();
    if (modal) modal.hide();
}

/**
 * Shows an HTML fragment which contains a data set modal.
 *
 * @param {String} id the identifier of the modal (DOM element)
 * @param {String} html the modal
 */
Application.loadModal = function (id, html) {
    Logger.debug(html);
    $('#' + id).remove();
    $(document.body).append(html);
    let modal = new bootstrap.Modal('#' + id, {});
    modal.show();
    this.registerModal(modal);
}

/**
 * Registers a modal, which is used to validate if the user clicks outside the popup.
 * @param {bootstrap.Modal} modal the modal instance
 */
Application.registerModal = function (modal) {
    this.getModals().push(modal);
}

/**
 * Returns the registers modals.
 *
 * @return {bootstrap.Modal[]} the modals.
 */
Application.getModals = function () {
    this.modals = this.modals || [];
    return this.modals;
}

/**
 * Executes a given action.
 *
 * @param {String} eventOrHandler the function to be called (handler) or the event to fire
 * @param {{}} [arguments] the arguments passed to the action listener
 */
Application.action = function (eventOrHandler) {
    if (Utils.isEmpty(eventOrHandler)) throw new Error("An event or a handler/function is required");
    let args = Array.prototype.slice.call(arguments, 1);
    if (Utils.isFunction(Application[eventOrHandler])) {
        Logger.info("Invoke handler '" + eventOrHandler + "'");
        Application[eventOrHandler].apply(this, args);
    } else {
        args.unshift(eventOrHandler);
        Application.fire.apply(this, args);
    }
}

/**
 * Registers an event listener.
 *
 * @param {String} name the event name
 * @param {Function} callback the function to be called when the event it is triggered.
 */
Application.bind = function (name, callback) {
    Logger.debug("Bind event " + name);
    this.listeners = this.listeners || {};
    this.listeners[name] = this.listeners[name] || []
    this.listeners[name].push(callback);
}

/**
 * Registers an event listener.
 *
 * @param {String} name the event name
 * @param {Function} [callback] the function to be called when the event it is triggered.
 */
Application.unbind = function (name, callback) {
    Logger.debug("Bind event " + name);
    let listeners = this.getListeners(name);
    let index = listeners.indexOf(callback);
    if (index !== -1) listeners.splice(index, 1);
}

/**
 * Fires an event to all listeners.
 *
 * @param {String} name the event name
 * @param {{}} [arguments] the arguments to be passed to the event callback.
 */
Application.fire = function (name) {
    let me = this;
    let args = Array.prototype.slice.call(arguments, 1);
    Logger.debug("Fire event " + name + ", arguments " + Utils.toString(args));
    let listeners = this.getListeners(name);
    for (const listener of listeners) {
        setTimeout(function () {
            listener.apply(me, args);
        }, 5);
    }
}

/**
 * Returns the listeners associated with an event.
 *
 * @param {String} name the name of the event
 * @return {Function[]} an array of listeners
 */
Application.getListeners = function (name) {
    this.listeners = this.listeners || {};
    this.listeners[name] = this.listeners[name] || [];
    return this.listeners[name];
}

/**
 * Initialize various global events
 */
Application.initEvents = function () {
    let me = this;
    $(document).on('click touchend', function (e) {
        let target = $(e.target);
        let located = false;
        me.getPopups().forEach(function (popup) {
            if (target.is(popup)) located = true;
        });
        if (!located) me.closePopups();
    });
}

/**
 * Initializes the application
 */
Application.initialize = function () {
    Logger.debug("Initialize application, request path '" + REQUEST_PATH + "', request arguments '" + Utils.toString(REQUEST_QUERY) + "'");
    this.initEvents();
}


// initialize application
Application.initialize();


