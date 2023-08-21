$(document).on({
    ajaxStart: function () {
        $("#ajax").show();
    },
    ajaxStop: function () {
        $("#ajax").hide();
    },
    ajaxError: function (event, jqxhr, settings, exception) {
        let json = {"error": "Internal server error"};
        try {
            json = JSON.parse(jqxhr.responseText);
        } catch (e) {
            // ignore
        }
        if (jqxhr.status === 400) {
            DataSet.showErrorAlert("Request", "A request (" + json.path + ") has invalid data");
        } else if (jqxhr.status === 401) {
            DataSet.showErrorAlert("Request", "A request (" + json.path + ") is not authorized");
        } else {
            DataSet.showErrorAlert("Request", "A request (" + json.path + "' failed with an error '" + json.error + "'");
        }
    }
});