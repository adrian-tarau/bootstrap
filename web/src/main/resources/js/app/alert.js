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