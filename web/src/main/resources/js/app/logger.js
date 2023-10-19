/*
* The Logger Global Variables
 */
window.Logger = window.Logger || {};

/**
 * Logs a message with a given logging level.
 *
 * @param {String }level the level/priority
 * @param {String }message the message
 */
Logger.log = function (level, message) {
    if (message && console) {
        if (!level || !(level in console)) {
            level = 'log';
        }
        message = '[' + level.toUpperCase() + '] ' + message;
        console[level](message);
    }
}

/**
 * Logs a message with a DEBUG level.
 * @param {String} message the message to log
 */
Logger.debug = function (message) {
    this.log('debug', message);
}

/**
 * Logs a message with a INFO level.
 * @param {String} message the message to log
 */
Logger.info = function (message) {
    this.log('info', message);
};

/**
 * Logs a message with a WARN level.
 * @param {String} message the message to log
 */
Logger.warn = function (message) {
    this.log('warn', message);
}

/**
 * Logs a message with a ERROR level.
 * @param {String} message the message to log
 */
Logger.error = function (message) {
    this.log('error', message);
}

