Application.bind("user.generate_token", function () {
    Logger.info("Generated token for '" + DataSet.getId() + "'");
    DataSet.post(DataSet.getId()+"/generate_token", {}, function (json) {
        Application.showInfoAlert("Token", json.message)
    }, {dataType: "json"});
});