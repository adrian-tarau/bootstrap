/*
* The Logger Global Variables
 */
window.Logger = window.Logger || {};

/**
 * Logs a message with a given logging level.
 *
 * @param {String} level the level/priority
 * @param {String} message the message
 */
Logger.log = function (level, message) {
    if (level === 'trace' && !this.traceEnabled) return;
    if (level === 'debug' && !this.debugEnabled) return;
    if (message && console) {
        if (!level || !(level in console)) {
            level = 'log';
        }
        message = '[' + level.toUpperCase() + '] ' + message;
        console[level](message);
    }
}

/**
 * Logs a message with a TRACE level.
 *
 * @param {String} message the message to log
 */
Logger.trace = function (message) {
    this.log('trace', message);
}

/**
 * Logs a message with a DEBUG level.
 *
 * @param {String} message the message to log
 */
Logger.debug = function (message) {
    this.log('debug', message);
}

/**
 * Logs a message with a INFO level.
 *
 * @param {String} message the message to log
 */
Logger.info = function (message) {
    this.log('info', message);
};

/**
 * Logs a message with a WARN level.
 *
 * @param {String} message the message to log
 */
Logger.warn = function (message) {
    this.log('warn', message);
}

/**
 * Logs a message with a ERROR level.
 *
 * @param {String} message the message to log
 */
Logger.error = function (message) {
    this.log('error', message);
}

/**
 * Enables or disables logging with DEBUG level.
 *
 * @param {boolean} enabled true to enable debug, false otherwise
 */
Logger.setEnableDebug = function (enabled) {
    Logger.debugEnabled = enabled;
}

/**
 * Enables or disables logging with TRACE level.
 *
 * @param {boolean} enabled true to enable debug, false otherwise
 */
Logger.setTraceDebug = function (enabled) {
    Logger.traceEnabled = enabled;
}

