/*
* The Application Global Variables
 */
window.Settings = window.Settings || {};

/**
 * Loads the configuration fields for a given element.
 *
 * @param {Element} el the DOM element
 */
Settings.load = function (el) {
    Utils.requireNonNull(el);
    let jqel = $(el);
    Settings.el = jqel;
    // deselects all items from navigation, select current item
    $("#settings_menu .list-group-item-action").removeClass("active");
    jqel.addClass("active");
    $("#settings_field_title").text(jqel.text());
    // extracts the group id (key) and loads the configuration fields for the group
    let groupId = jqel.data("group-id");
    Logger.info("Load fields for '" + groupId + "'");
    Utils.requireNonNull(groupId);
    Application.get("fields/" + groupId, {}, function (data) {
        $("#settings_fields").html(data);
    });
}

/**
 * Saves the configuration fields.
 */
Settings.save = function () {
    let groupId = this.el.data("group-id");
    Logger.info("Load fields for '" + groupId + "'");
    Utils.requireNonNull(groupId);
    Application.saveForm("#settings_fields", groupId, {}, {
        success: function (data) {
            Application.showInfoAlert("Configuration", "Entries saved successfully, " + data.message);
        }
    });
}