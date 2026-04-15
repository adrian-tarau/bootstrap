/*
* The Application Global Variables
 */
window.Configuration = window.Configuration || {};

/**
 * Returns the configuration value.
 *
 * @param {string} name the name of the configuration entry
 * @param {string|number} defaultValue the default value of the configuration entry
 *
 * @return {string|number} the value of the configuration
 */
Configuration.get = function (name, defaultValue) {
    if (Utils.isEmpty(name)) throw new Error("The configuration key/name is not defined");
    if (!this.entries) this.entries = {};
    return this.entries[name] ?? defaultValue;
}

/**
 * Initializes the configuration from a map.
 *
 * @param {object} entries an object/map containing the configuration entries
 */
Configuration.initialized = function (entries) {
    Utils.requireNonNull(entries);
    this.entries = entries;
    Logger.debug("Loaded " + Object.keys(entries).length + " configuration entries");
}

