Application.bind("user.generate_token", function () {
    Logger.info("Generated token for '" + DataSet.getId() + "'");
    DataSet.post(DataSet.getId()+"/generate_token", {}, function (json) {
        Application.showAlert("Token", json.message, {
            position: 'center',
            drag : false,
            maxWidth: 600,
            timeout: false,
        })
    }, {dataType: "json"});
});

Application.bind("user.reset_password", function () {
    Logger.info("Reset password for '" + DataSet.getId() + "'");
    DataSet.post(DataSet.getId() + "/reset_password", {}, function (json) {
        Application.showAlert("Password", json.message, {
            position: 'center',
            drag : false,
            timeout: false
        })
    }, {dataType: "json"});
});