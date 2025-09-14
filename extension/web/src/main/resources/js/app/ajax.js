$(document).on({
    ajaxStart: function () {
        $("#ajax").show();
    },
    ajaxStop: function () {
        $("#ajax").hide();
    },
    ajaxError: function (event, jqxhr, settings, exception) {
        let json = {"error": exception ? exception : "Internal server error"};
        try {
            json = JSON.parse(jqxhr.responseText);
        } catch (e) {
            // ignore
        }
        if (!Utils.isDefined(json.errorCode)) json.errorCode = 0;
        if (jqxhr.status === 0) {
            // communication problems, do not show any alert
        } else if (jqxhr.status === 400) {
            if (json.errorCode === 24) {
                Application.showWarnAlert("Abort", json.message);
            } else {
                Application.showErrorAlert("Request", "A request (" + json.path + ") has invalid data");
            }
        } else if (jqxhr.status === 401) {
            Application.showErrorAlert("Request", "A request (" + json.path + ") is not authorized");
        } else if (jqxhr.status === 403) {
            Application.showErrorAlert("Request", "A request (" + json.path + ") is forbidden");
        } else {
            Application.showErrorAlert("Request", "A request (" + json.path + "') failed with an error '" + json.error + "'");
        }
    }
});