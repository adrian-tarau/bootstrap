/**
 * Constants for alert types
 * @type {string}
 */
const ALERT_TYPE_INFO = "INFO";
const ALERT_TYPE_WARN = "WARN";
const ALERT_TYPE_ERROR = "ERROR";

/**
 * Shows an informational message.
 *
 * @param {String} title the title
 * @param {String} message the message to display, it can contain HTML tags
 */
Application.showInfoAlert = function (title, message) {
    Application.showAlert(title, message, ALERT_TYPE_INFO);
}

/**
 * Shows a warning message.
 *
 * @param {String} title the title
 * @param {String} message the message to display, it can contain HTML tags
 */
Application.showWarnAlert = function (title, message) {
    Application.showAlert(title, message, ALERT_TYPE_WARN);
}

/**
 * Shows an error message.
 *
 * @param {String} title the title
 * @param {String} message the message to display, it can contain HTML tags
 */
Application.showErrorAlert = function (title, message) {
    Application.showAlert(title, message, ALERT_TYPE_ERROR);
}

/**
 * Shows an alert.
 *
 * @param {String} title the title
 * @param {String} message the message to display, it can contain HTML tags
 * @param {String} type the type of alert
 */
Application.showAlert = function (title, message, type) {
    type = type || "INFO";
    let icon = "fa-solid fa-circle-info";
    let color = "green";
    switch (type) {
        case 'WARN' :
            icon = "fa-solid fa-triangle-exclamation";
            color = "yellow";
            break
        case 'ERROR' :
            icon = "fa-solid fa-circle-xmark";
            color = "red";
            break
    }
    iziToast.show({
        title: title,
        message: message,
        icon: icon,
        close: true,
        timeout: 5000,
        position: 'topRight',
        color: color
    });
}

/**
 * Shows a tooltip on an element.
 *
 * @param {Object|String} element the element reference
 * @param {String} message the message to display
 */
Application.showTooltip = function (element, message) {
    if ($(element).length === 0) {
        Logger.error("An element with identifier '" + element + "' does not exist");
    } else {
        let tooltip = new bootstrap.Tooltip($(element), {
            title: message,
            placement: 'right',
            trigger: 'manual',
            delay: {"show": 0, "hide": 2000},
            customClass: 'alert-danger'
        });
        tooltip.show();
    }
}

/**
 * Hides a tooltip on an element.
 *
 * @param {Object|String} element the element reference
 */
Application.hideTooltip = function (element) {
    if ($(element).length === 0) {
        Logger.error("An element with identifier '" + element + "' does not exist");
    } else {
        $(element).tooltip("dispose");
    }
}