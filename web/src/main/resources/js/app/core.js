/*
* The Application Global Variables
 */
window.Application = window.Application || {};
window.Application.MODAL_Z_INDEX = 1050;

/**
 * Default params for ajax requests.
 */
const APP_AJAX_DEFAULT_OPTIONS = {self: true, params: false};
const APP_AJAX_DEFAULT_TIMEOUT = 120 * 1000;

/**
 * Returns the path of the current request.
 * @return {string}
 */
Application.getPath = function () {
    return APP_REQUEST_PATH || "/";
}

/**
 * Takes a collection of parameters and creates an object with all query parameters.
 *
 * @param {Object} [params] a collection of parameters to override/extend the request parameters
 */
Application.getQuery = function (params) {
    let requestParams = $.extend({}, APP_REQUEST_QUERY || null);
    requestParams = $.extend(requestParams, params);
    return requestParams;
}

/**
 * Returns an application URI.
 *
 * @param {String} path an optional path to add to the base URI
 * @param {Object} [params] an optional list of parameters
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.self=true] an optional boolean, to calculate the URI to the current page (within the same resource)
 * @param {Boolean} [options.params=false] an optional boolean, to include the parameters in the URI
 */
Application.getUri = function (path, params, options) {
    options = options || APP_AJAX_DEFAULT_OPTIONS;
    options.self = Utils.isDefined(options.self) ? options.self : true;
    options.params = Utils.isDefined(options.params) ? options.params : false;
    params = options.params ? this.getQuery(params) : params;
    let uri = options.self ? this.getPath() : "/";
    if (path) {
        if (!uri.endsWith("/")) uri += "/";
        if (path.startsWith("/")) path = path.substring(1);
        uri += path;
    }
    if (Utils.isNotEmpty(params)) uri += "?" + $.param(params);
    Logger.debug("Resolve URI for path '" + path + "', params '" + Utils.toString(params) + "', uri '" + uri + "'");
    return uri;
}

/**
 * Opens an application page, using the same path (or a sub-path) as the current page and possible
 * additional parameters.
 *
 * @param {String} path an optional path to add to the base URI
 * @param {Object} params the new parameters (overrides)
 */
Application.openSelf = function (path, params) {
    let uri = this.getUri(path, params, {self: true, params: true});
    Logger.info("Open '" + uri + "'");
    window.location.href = uri;
}

/**
 * Takes a collection of parameters and opens a page at requested path, outside the current page.
 *
 * @param {Object} params the new parameters
 * @param {String} [path] an optional path to add to the base URI
 * @param {Boolean} [newWindow=false] an optional flag, true to control if the URI is opened in a new window, false otherwise
 */
Application.open = function (path, params, newWindow) {
    let uri = this.getUri(path, params, {self: false});
    newWindow = Utils.defaultIfNotDefined(newWindow, false);
    Logger.info("Open '" + uri + "', new window '" + newWindow + "'");
    window.open(uri, newWindow ? "_blank" : "");
}

/**
 * Returns whether the application is ready.
 * @returns true if ready, false otherwise
 */
Application.isReady = function () {
    return true;
}

/**
 * Reloads the current page.
 */
Application.reload = function () {
    this.openSelf("", {});
}

/**
 * Executes an AJAX GET request.
 *
 * @param {String} path the path
 * @param {Object} params the parameters
 * @param {Function} callback the callback to be called with the successful response
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.self=true] an optional boolean, to calculate the URI relative to the current page (self)
 * @param {Boolean} [options.params=false] an optional boolean, to include the parameters in the current URI
 * @param {String} [options.dataType=text] an optional string, to provide a data type for the response
 * @param {Boolean} [options.mask] an optional DOM selector, which will be masked while the request is running
 * @param {Boolean} [options.error] an optional function, to be called if the request fails
 * @param {Boolean} [options.complete] an optional function, to be called when the request ends (successful or not)
 */
Application.get = function (path, params, callback, options) {
    Application.ajax('GET', path, params, callback, options);
}

/**
 * Executes an AJAX POST request.
 *
 * @param {String} path the path
 * @param {Object} params the parameters
 * @param {Function} callback the callback to be called with the successful response
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.self=true] an optional boolean, to calculate the URI relative to the current page (self)
 * @param {Boolean} [options.params=false] an optional boolean, to include the parameters in the current URI
 * @param {Boolean} [options.dataType=text] an optional string, to provide a data type for the response
 * @param {Boolean} [options.contentType] the content type of the request, defaults to "application/x-www-form-urlencoded; charset=UTF-8" for a POST
 * @param {Boolean} [options.mask] an optional DOM selector, which will be masked while the request is running
 * @param {Boolean} [options.error] an optional function, to be called if the request fails
 * @param {Boolean} [options.complete] an optional function, to be called when the request ends (successful or not)
 */
Application.post = function (path, params, callback, options) {
    Application.ajax('POST', path, params, callback, options);
}

/**
 * Executes an AJAX DELETE request.
 *
 * @param {String} path the path
 * @param {Object} params the parameters
 * @param {Function} callback the callback to be called with the successful response
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.self=true] an optional boolean, to calculate the URI relative to the current page (self)
 * @param {Boolean} [options.params=false] an optional boolean, to include the parameters in the current URI
 * @param {Boolean} [options.dataType=text] an optional string, to provide a data type for the response
 * @param {Boolean} [options.mask] an optional DOM selector, which will be masked while the request is running
 * @param {Boolean} [options.error] an optional function, to be called if the request fails
 * @param {Boolean} [options.complete] an optional function, to be called when the request ends (successful or not)
 */
Application.delete = function (path, params, callback, options) {
    Application.ajax('DELETE', path, params, callback, options);
}

/**
 * Executes an AJAX request.
 *
 * @param {String} type the HTTP verb
 * @param {String} path the path
 * @param {Object} params the parameters
 * @param {Function} callback the callback to be called with the successful response
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.self=true] an optional boolean, to calculate the URI relative to the current page (self)
 * @param {Boolean} [options.params=false] an optional boolean, to include the parameters in the current URI
 * @param {Boolean} [options.dataType=text] an optional string, to provide a data type for the response
 * @param {Boolean} [options.data=text] the body of a POST
 * @param {Boolean} [options.contentType] the content type of the request, defaults to "application/x-www-form-urlencoded; charset=UTF-8" for a POST
 * @param {String} [options.mask] an optional DOM selector, which will be masked while the request is running
 * @param {String} [options.maskMessage] an optional message to display while masking, defaults to no message
 * @param {Boolean} [options.error] an optional function, to be called if the request fails
 * @param {Boolean} [options.complete] an optional function, to be called when the request ends (successful or not)
 */
Application.ajax = function (type, path, params, callback, options) {
    const me = this;
    options = options || {};
    options.self = Utils.isDefined(options.self) ? options.self : true;
    options.params = Utils.isDefined(options.params) ? options.params : false;
    options.error = Utils.defaultIfNotDefinedOrNull(options.error, function () {
        // nothing to do for errors
    });
    options.dataType = Utils.defaultIfNotDefinedOrNull(options.dataType, "text");
    params = options.params ? this.getQuery(params) : params;
    options.params = false;
    type = Utils.defaultIfNotDefinedOrNull(type, "GET");
    let uri = this.getUri(path, {}, options);
    let data = type === 'POST' && Utils.isNotEmpty(options.data) ? options.data : params;
    Logger.info("Ajax Request: " + uri + ", params: " + Utils.toString(params) + ", data type " + options.dataType);
    if (options.mask) me.mask(options.mask, options.maskMessage);
    $.ajax({
        url: uri,
        type: type,
        data: data,
        dataType: options.dataType,
        contentType: Utils.defaultIfNotDefinedOrNull(options.contentType, "application/x-www-form-urlencoded; charset=UTF-8"),
        timeout: APP_AJAX_DEFAULT_TIMEOUT,
        headers: {"X-TimeZone": Application.getTimezoneOffset()},
        success: function (output, status, xhr) {
            if (options.complete) options.complete.apply(this, arguments);
            if (options.mask) me.unmask(options.mask);
            callback.apply(this, [output, status, xhr]);
        },
        error: function (xhr, status, error) {
            if (options.error) options.error.apply(this, arguments);
            if (options.complete) options.complete.apply(this, arguments);
            if (options.mask) me.unmask(options.mask);
        }
    });
}

/**
 * Returns the query parameter with a given name.
 *
 * @param {String} name the parameter name
 * @return {String} the value of the parameter
 */
Application.getQueryParam = function (name) {
    let url_string = location.href;
    let url = new URL(url_string);
    return url.searchParams.get(name);
}

/**
 * Returns the hash path from the current URI.
 *
 * @return {String} the path from has
 */
Application.getHashPath = function () {
    let url = this.getHashUrl();
    return url.pathname;
}

/**
 * Returns the hash parameter with a given name.
 *
 * @param {String} name the parameter name
 * @return {String} the value of the parameter
 */
Application.getHashParam = function (name) {
    let url = this.getHashUrl();
    return url.searchParams.get(name);
}

/**
 * Returns an URL made out of the hash (as path and params)
 * @return {URL} the URL
 */
Application.getHashUrl = function () {
    let hash = location.hash;
    hash = hash && (hash.charAt(0) === '#') ? hash.slice(1) : hash;
    if (!hash.startsWith("/")) hash = "/" + hash;
    return new URL("http://localhost" + hash);
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
    if (modal) {
        Application.MODAL_Z_INDEX -= 10;
        modal.hide();
    }
}

/**
 * Shows a dialog (modal) which contains the HTML fragment.
 *
 * @param {String} id the identifier of the modal (DOM element)
 * @param {String} content the contained of the modal (HTML, Text, etc)
 * @return {bootstrap.Modal} the modal
 */
Application.loadModal = function (id, content) {
    let me = Application;
    Utils.requireNonNull(id, "id")
    me.removeModal(id);
    let selector = '#' + id;
    $(document.body).append(content);
    let modalElement = $(selector);
    if (modalElement.length === 0) {
        throw new Error("A modal with id '" + id + "' does not exist in the DOM");
    }
    // remove the DOM element after the hide transition finishes
    modalElement.on('hidden.bs.modal', function () {
        me.removeModal(id);
    })
    let modal = new bootstrap.Modal(selector, {});
    modal.show();
    this.registerModal(id, modal);
    tippy('[data-tippy-content]');
    return modal;
}

/**
 * @private
 * Removes the model element with a given id from the DOM.
 * @param {String} id the element id
 */
Application.removeModal = function (id) {
    Utils.requireNonNull(id, "id")
    $('#' + id).remove();
}

/**
 * Registers a modal, which is used to validate if the user clicks outside the popup.
 * @param {bootstrap.Modal} modal the modal instance
 */
Application.registerModal = function (id, modal) {
    this.getModals().push(modal);
    let selector = '#' + id;
    Application.MODAL_Z_INDEX += 10;
    let modalIndex = Application.MODAL_Z_INDEX;
    let modalBackdropIndex = Application.MODAL_Z_INDEX - 1;
    // Manually bump z-index of the modal and backdrop
    $(selector).css('z-index', modalIndex);
    $('.modal-backdrop').last().css('z-index', modalBackdropIndex);
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
 * Returns the time zone offset.
 *
 * @return {number} offset in minutes since UTC
 */
Application.getTimezoneOffset = function () {
    return new Date().getTimezoneOffset();
}

/**
 * Masks the element with a given selector.
 *
 * @param {String} [selector] the DOM selector to mask, if not provided the whole body will be masked
 * @param {String} [message] the message to display while masking, defaults to no message
 */
Application.mask = function (selector, message) {
    let options = {
        image: false,
        //text: message,
        fontawesome: "fa-solid fa-arrows-rotate",
        fontawesomeAnimation: 'rotate_right',
        minSize: 20,
        maxSize: 40
    }
    $.LoadingOverlaySetup(options);
    if (selector) {
        $(selector).LoadingOverlay("show");
    } else {
        $.LoadingOverlay("show");
    }
}

/**
 * Unmasks the element with a given selector.
 */
Application.unmask = function (selector) {
    if (selector) $(selector).LoadingOverlay("hide", true);
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
Application.bind = function (name, callback,) {
    if (!Utils.isFunction(callback)) {
        Logger.error("Callback is not a function for event '" + name + "'");
        return
    }
    let args = Array.prototype.slice.call(arguments, 2);
    Logger.debug("Bind event " + name + ", arguments: " + Utils.toString(args));
    this.listeners = this.listeners || {};
    this.listeners[name] = this.listeners[name] || []
    this.listeners[name].push(callback);
    this.listeners_args = this.listeners_args || {};
    this.listeners_args[name] = this.listeners_args[name] || []
    this.listeners_args[name] = args;
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
    let extraArgs = this.listeners_args[name] || [];
    args = args.concat(extraArgs);
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
 * Initialize time zone.
 */
Application.initTimeZone = function () {
    if (Utils.isNotEmpty(APP_TIME_ZONE)) return;
    Logger.info("Send client time zone '" + Application.getTimezoneOffset() + "'");
    Application.post("settings/session/time-zone", {}, function (data) {
        // nothing to process
    }, {self: false});
}

/**
 * Initializes the application
 */
Application.initialize = function () {
    Logger.debug("Initialize application, request path '" + this.getPath() + "', request arguments '" + Utils.toString(this.getQuery()) + "'");
    this.initEvents();
    this.initTimeZone();
}

/**
 * Starts the application.
 */
Application.start = function () {
    Logger.debug("Start application");
    this.fire("start");
}

// initialize application
Application.initialize();


