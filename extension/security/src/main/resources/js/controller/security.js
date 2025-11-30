Application.bind("user.generate_token", function () {
    Logger.info("Generated token for '" + DataSet.getId() + "'");
    DataSet.post(DataSet.getId()+"/generate_token", {}, function (json) {
        let buttons = [];
        if (Utils.isDefined(json.attributes) && Utils.isNotEmpty(json.attributes.apiKey)) {
            buttons = [
                ['<button>Copy</button>', async function (instance, toast) {
                    await Application.copyToClipboard(json.attributes.apiKey, "API Key");
                }, true]
            ];
        }
        Application.showAlert("Token", json.message, {
            timeout: false,
            buttons: buttons,
        })
    }, {dataType: "json"});
});

Application.bind("user.reset_password", function () {
    Logger.info("Reset password for '" + DataSet.getId() + "'");
    DataSet.post(DataSet.getId() + "/reset_password", {}, function (json) {
        let buttons = [];
        if (Utils.isDefined(json.attributes) && Utils.isNotEmpty(json.attributes.password)) {
            buttons = [
                ['<button>Copy</button>', async function (instance, toast) {
                    await Application.copyToClipboard(json.attributes.password, "Password");
                }, true]
            ];
        }
        Application.showAlert("Password", json.message, {
            timeout: false,
            buttons: buttons,
        })
    }, {dataType: "json"});
});