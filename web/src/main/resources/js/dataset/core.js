/*
* The Data Set namespace
 */
var DataSet = DataSet || {};

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters
 * @param {Object }params the new parameters
 */
DataSet.open = function (params) {
    let requestParams = $.extend({}, REQUEST_QUERY);
    requestParams = $.extend(requestParams, params);
    let uri = REQUEST_PATH + "?" + $.param(requestParams);
    console.info("Open dat set " + uri);
    window.location.href = uri;
}

/**
 * Loads using AJAX the next page for a data set
 * @param {Integer} page the page number, first one starts at 1
 */
DataSet.loadPage = function (page) {

}

/**
 * Loads using AJAX the next page for a data set
 * @param {String} action the action to perform
 */
DataSet.performAction = function (action) {
    if (!DataSet.id) throw new Error("An identifier for the current model is not registered")
    if (action === "view") {
        DataSet.view();
    } else if (action === "edit") {
        DataSet.edit();
    } else if (action === "delete") {
        DataSet.delete();
    }
}

/**
 * Views the current model.
 */
DataSet.view = function () {
    $.get(REQUEST_PATH + "/" + DataSet.id + "/view", function (data) {
        DataSet.loadDialog(data);
    });
}

/**
 * Edit the current model.
 */
DataSet.edit = function () {
    $.get(REQUEST_PATH + "/" + DataSet.id + "/edit", function (data) {
        DataSet.loadDialog(data);
    });
}

/**
 * Delete the current model.
 */
DataSet.delete = function () {
    $.get(REQUEST_PATH + "/" + DataSet.id + "/delete", function (data) {
        DataSet.loadDialog(data);
    });
}

/**
 * Shows an HTML fragment which contains a data set modal.
 * @param {String} html the modal
 */
DataSet.loadDialog = function (html) {
    console.log(html);
    $('#dataset-model').remove();
    $(document.body).append(html);
    let model = new bootstrap.Modal('#dataset-dialog', {});
    model.show();
}

/**
 * Shows the actions dropdown.
 *
 * @param {EventSource} event the click event
 * @param {String} id the model identifier
 */
DataSet.showActions = function (event, id) {
    event.stopPropagation();
    DataSet.closePopups();
    DataSet.id = id;
    let actions = $('#dataset-actions');
    let element = $(event.target);
    if (DataSet.popper) {
        DataSet.popper.destroy();
    }
    DataSet.popper = Popper.createPopper(element[0], actions[0], {
        placement: 'bottom'
    });
    DataSet.popper.update();
    DataSet.registerPopup(actions);
    actions.show();
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters
 * @param {String}field the new parameters
 * @param {String}direction the new parameters
 */
DataSet.sort = function (field, direction) {
    let requestParams = $.extend({}, REQUEST_QUERY);
    let sort = field + "=" + direction;
    requestParams["sort"] = sort;
    DataSet.open(requestParams);
}

/**
 * Initialize global events.
 */
DataSet.initEvents = function () {
    $(document).on('click touchend', function (e) {
        let target = $(e.target);
        let located = false;
        DataSet.getPopups().forEach(function (popup) {
            if (target.is(popup)) located = true;
        });
        if (!located) {
            DataSet.closePopups();
        }
    });
}

/**
 * Closes all open popups.
 */
DataSet.closePopups = function () {
    DataSet.getPopups().forEach(function (popup) {
        popup.hide();
    });
    DataSet.popups = [];
}

/**
 * Registers a popup, which is used to validate if the user clicks outside the popup.
 * @param {Object} element a
 */
DataSet.registerPopup = function (element) {
    DataSet.getPopups().push(element);
}

/**
 * Returns the registers popups.
 *
 * @return {Object[]} the popups.
 */
DataSet.getPopups = function () {
    DataSet.popups = DataSet.popups || [];
    return DataSet.popups;
}

/**
 * Initializes the data set.
 */
DataSet.init = function () {
    DataSet.initEvents();
}

DataSet.init();