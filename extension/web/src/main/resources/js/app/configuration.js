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

/**
 * Loads the configuration fields for a given element.
 *
 * @param {Element} el the DOM element
 */
Configuration.load = function (el) {
    Utils.requireNonNull(el);
    let jqel = $(el);
    $("#settings_menu .list-group-item-action").removeClass("active");
    jqel.addClass("active");
    $("#settings_field_title").text(jqel.text());

    let groupId = jqel.data("group-id");
    Utils.requireNonNull(groupId);
    Logger.info("Load fields for '" + groupId + "'");
    Application.get("fields/" + groupId, {}, function (data) {
        $("#settings_fields").html(data);
    });
}