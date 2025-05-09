/*
* The Utilities Global Variables
 */
window.Utils = window.Utils || {};
window.Utils.ID_GENERATOR = 1;

/**
 * Returns whether the passed value is empty. The value is deemed to be empty if it is either:
 *
 * - `null`
 * - `undefined`
 * - a zero-length array
 * - a zero-length string
 *
 * @param {Object} value The value to test.
 * @return {Boolean} true if empty, false otherwise
 */
Utils.isEmpty = function (value) {
    return (value == null) || (value === '') || (Utils.isArray(value) && value.length === 0);
}

/**
 * Returns whether the passed value is not empty.
 *
 * @param {Object} value The value to test.
 * @return {Boolean} true if empty, false otherwise
 * @see Utils.isEmpty
 */
Utils.isNotEmpty = function (value) {
    return !Utils.isEmpty(value);
}

/**
 * Returns whether the passed value is a JavaScript Array.
 *
 * @param {Object} value the value to test.
 * @return {Boolean} true if an array, false otherwise
 */
Utils.isArray = function (value) {
    return toString.call(value) === '[object Array]';
}

/**
 * Returns whether the passed value is a JavaScript Object.
 *
 * @param {Object} value the value to test.
 * @return {Boolean} true if an object, false otherwise
 */
Utils.isObject = function (value) {
    return value !== null && value !== undefined && toString.call(value) === '[object Object]' && value.ownerDocument === undefined;
}

/**
 * Returns whether the passed value is a JavaScript Function.
 * @param {Object} value the value to test.
 * @return {Boolean} true if a function, false otherwise
 */
Utils.isFunction = function (value) {
    return !!value && toString.call(value) === '[object Function]';
}

/**
 * Returns whether the passed value is a JavaScript 'primitive', a string, number or boolean.
 *
 * @param {Object} value the value to test.
 * @return {Boolean} true if a primitive, false otherwise
 */
Utils.isPrimitive = function (value) {
    let type = typeof value;
    return type === 'string' || type === 'number' || type === 'boolean';
}

/**
 * Returns whether the passed value is a number.
 *
 * @param {Object} value the value to test.
 * @return {Boolean} true if number, false otherwise
 */
Utils.isNumber = function (value) {
    return typeof value === 'number' && isFinite(value);
}

/**
 * Validates whether that a value is numeric.
 *
 * @param {Object} value the value to test
 * @return {Boolean} true if numeric, false otherwise
 */
Utils.isNumeric = function (value) {
    return !isNaN(parseFloat(value)) && isFinite(value);
}

/**
 * Returns whether the passed value is a string.
 *
 * @param {Object} value the value to test.
 * @return {Boolean} true if string, false otherwise
 */
Utils.isString = function (value) {
    return typeof value === 'string';
}

/**
 * Returns whether the passed value is a boolean.
 *
 * @param {Object} value the value to test.
 * @return {Boolean} true if boolean, false otherwise
 */
Utils.isBoolean = function (value) {
    return typeof value === 'boolean';
}

/**
 * Returns whether the passed value is defined.
 * @param {Object} value the value to test.
 * @return {Boolean} true if defined, false otherwise
 */
Utils.isDefined = function (value) {
    return typeof value !== 'undefined';
}

/**
 * Returns whether the value if defined or the default value.
 *
 * @param {*} value the value to test.
 * @param {*} defaultValue the default value to test.
 * @return {*}
 */
Utils.defaultIfNotDefined = function (value, defaultValue) {
    return Utils.isDefined(value) ? value : defaultValue;
}

/**
 * Returns whether the value if defined or the default value.
 *
 * @param {*} value the value to test.
 * @param {*} defaultValue the default value to test.
 * @return {*}
 */
Utils.defaultIfNotDefinedOrNull = function (value, defaultValue) {
    return Utils.isDefined(value) && value != null ? value : defaultValue;
}

/**
 * Returns an array if the value is not an array already.
 *
 * @param {Object} value the value to convert
 * @returns {*[]} the array
 */
Utils.toArray = function (value) {
    if (!this.isArray(value)) value = [value];
    return value;
}

/**
 * Validates that the reference is not undefined or not null.
 *
 * @param {Object} reference a reference
 */
Utils.requireNonNull = function (reference) {
    if (Utils.isEmpty(reference)) throw new Error("Reference cannot be undefined or NULL");
}

/**
 * Defers the execution of the callback by a number of milliseconds.
 *
 * @param {Function} callback the callback
 * @param {Number} [delay] the delay in milliseconds
 * @param {Object} [self] the self
 */
Utils.defer = function (callback, delay, self) {
    if (!this.isDefined(delay)) delay = 0;
    setTimeout(callback, delay);
}

/**
 * Formats millis as hh:mm:ss.millis.
 *
 * @param {int} value the value in millis
 * @param {boolean} [units] true to be displayed with units, false to be displayed as clock time
 * @return {string} the formatted value
 */
Utils.formatMillis = function (value, units) {
    if (!Utils.isDefined(units)) units = true;

    // Pad to 2 or 3 digits, default is 2
    function pad(n, z) {
        z = z || 2;
        return ('00' + n).slice(-z);
    }

    value = Math.abs(value);
    let ms = value % 1000;
    let msp = pad(ms, 3);
    value = (value - ms) / 1000;
    let secs = value % 60;
    let secsp = pad(secs);
    value = (value - secs) / 60;
    let mins = value % 60;
    let minsp = pad(mins);
    let hrs = (value - mins) / 60;
    let displayValue;
    if (units) {
        if (hrs === 0) {
            if (mins === 0) {
                if (secs === 0) {
                    displayValue = ms + "ms";
                } else {
                    displayValue = secs + 's ' + ms + "ms";
                }
            } else {
                displayValue = mins + 'm ' + secs + 's ' + ms + "ms";
            }
        } else {
            displayValue = hrs + 'h ' + mins + 'm ' + secs + 's ' + msp+ "ms";
        }
    } else {
        if (hrs === 0) {
            if (mins === 0) {
                displayValue = secsp + '.' + msp;
            } else {
                displayValue = minsp + ':' + secsp + '.' + msp;
            }
        } else {
            displayValue = pad(hrs) + ':' + minsp + ':' + secsp + '.' + msp;
        }
    }
    return displayValue;
}

/**
 * Returns the string representation of the reference.
 *
 * @param {Object} value a JavaScript reference
 * @param {Boolean} [quoteStrings] true to quote strings, false otherwise
 * @returns {String} the string representation of an object
 */
Utils.toString = function (value, quoteStrings) {
    quoteStrings = quoteStrings || true;
    if (value === undefined) {
        return "#undefined";
    } else if (value === null) {
        return "#null";
    } else if (value instanceof Error) {
        return "Error(message=" + value.message + ", stack=" + value.stack + ")";
    } else if (Utils.isFunction(value.catch) && Utils.isFunction(value.then)) {
        let buffer = '{Promise ';
        buffer += "}"
        return buffer;
    } else if (Utils.isFunction(value)) {
        return "#FN()"
    } else if (Utils.isArray(value)) {
        let buffer = '[';
        for (const item in value) {
            if (buffer.length > 2) buffer += ", ";
            buffer += Utils.toString(item);
        }
        buffer += "]"
        return buffer;
    } else if (Utils.isObject(value)) {
        let buffer = '{';
        let maxProperties = 10;
        let truncatedProperties = 0;
        for (const [p, v] of Object.entries(value)) {
            if (maxProperties >= 0) {
                if (buffer.length > 2) buffer += ", ";
                if (Utils.isFunction(v) || Utils.isObject(v)) {
                    buffer += p + "=" + this.toString(v);
                } else {
                    buffer += p + "=" + v;
                }
            }
            if (maxProperties-- < 0) {
                truncatedProperties++;
            }
        }
        if (maxProperties < 0) buffer += ",truncated=" + truncatedProperties;
        buffer += "}"
        return buffer;
    } else if (Utils.isString(value)) {
        let c = value.trim().charAt(0);
        if (!(c === '\'' || c === '\"' || !quoteStrings)) {
            value = "'" + value + "'";
        }
        return value;
    } else {
        return value;
    }
}

/**
 * Intercepts an object and creates anonymous function for properties which look like functions.
 *
 * @param  {object} value the object to intercept
 * @param {Function} [interceptor] an optional interceptor of a string value
 * @return {object} the intercepted object
 */
Utils.intercept = function (value, interceptor) {
    const me = this;
    if (value === undefined || value === null) {
        return value;
    } else if (Utils.isArray(value)) {
        value.forEach(function (value, index, array) {
            array[index] = me.intercept(value, interceptor);
        });
        return value;
    } else if (Utils.isObject(value)) {
        for (const [p, v] of Object.entries(value)) {
            value[p] = me.intercept(v, interceptor);
        }
        return value;
    } else if (Utils.isString(value)) {
        if (value.match(/function(.*)\s{/g) != null) {
            value = eval("(" + value + ")");
        } else if (interceptor) {
            value = interceptor.call(interceptor, value)
        }
        return value;
    } else {
        return value;
    }
}

/**
 * Returns a unique identifier.
 *
 * The identifier will be unique across a page and can be used to create DOM element identifiers.
 *
 * @return {number} the identifier
 */
Utils.uuid = function () {
    return Utils.ID_GENERATOR++;
}

