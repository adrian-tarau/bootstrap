/*
* The Data Set Global Variables
 */
window.DataSet = window.DataSet || {};
window.REQUEST_PATH = window.REQUEST_PATH || "/";
window.REQUEST_QUERY = window.REQUEST_QUERY || null;

/**
 * Constants for alert yypes
 * @type {string}
 */
const DATASET_ALERT_TYPE_INFO = "INFO";
const DATASET_ALERT_TYPE_WARN = "WARN";
const DATASET_ALERT_TYPE_ERROR = "ERROR";
const DATE_RANGE_SEPARATOR = "|";

/**
 * A CSS class used to tag the element which host the drag and drop area for upload
 * @type {string}
 */
const DATASET_DROP_ZONE_CLASS = "dataset-drop-zone";

/**
 * Takes a collection of parameters and creates a URI (path + query parameters).
 *
 * @param {Object} params the new parameters
 * @param {String} [path] an optional path to add to the base URI
 */
DataSet.uri = function (params, path) {
    let requestParams = DataSet.query(params);
    let uri = REQUEST_PATH;
    if (path) {
        if (path.startsWith("/")) path.substring(1);
        uri += "/" + path;
    }
    uri += "?" + $.param(requestParams);
    console.info("Data Set URI: " + uri);
    return uri;
}

/**
 * Takes a collection of parameters and creates an object with all query parameters.
 *
 * @param {Object} params the new parameters
 */
DataSet.query = function (params) {
    let requestParams = $.extend({}, REQUEST_QUERY);
    requestParams = $.extend(requestParams, params);
    requestParams["query"] = $("#query").val();
    let timeFilter = DataSet.getTimeFilter();
    if (timeFilter.length > 0) {
        requestParams["range"] = timeFilter[0].toISOString() + DATE_RANGE_SEPARATOR + timeFilter[1].toISOString();
    }
    return requestParams;
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters
 *
 * @param {Object} params the new parameters
 * @param {String} [params] an optional path to add to the base URI
 */
DataSet.open = function (params, path) {
    window.location.href = DataSet.uri(params, path);
}

/**
 * Reloads the data set page.
 */
DataSet.reload = function () {
    DataSet.open({});
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters.
 *
 * @param {String} path the path
 * @param {Object} params the new parameters
 */
DataSet.ajax = function (path, params, callback) {
    let requestParams = DataSet.query(params);
    let uri = REQUEST_PATH + "/" + path;
    console.info("Ajax data set " + uri);
    $.get({
        data: requestParams,
        url: uri,
        success: function (output, status, xhr) {
            callback.apply(this, [output, status, xhr]);
        }
    });
}

/**
 * Triggers a query in the data set.
 * @param {String } query the query to execute, if empty
 */
DataSet.search = function (query) {
    if (!$.isEmptyObject(query)) $("#query").val(query);
    DataSet.open("");
    return false;
}

/**
 * Returns the query parameter with a given name.
 * @param {String} name the parameter name
 */
DataSet.getQueryParam = function (name) {
    let url_string = location.href;
    let url = new URL(url_string);
    let val = url.searchParams.get(arguments[0]);
    return val;
}

/**
 * Loads using AJAX the next page for a data set
 * @param {Integer} page the page number, first one starts at 1
 */
DataSet.loadPage = function (page) {
    page = parseInt(page);
    DataSet.ajax("page", {page: page}, function (data, status, xhr) {
        $("#dataset-grid tbody").append(data);
        $("#more_results").attr("data-page", page + 1);
        $("#page_info").text(xhr.getResponseHeader('X-DATASET-PAGE-INFO'));
        $("#page_info_and_records").text(xhr.getResponseHeader('X-DATASET-PAGE-INFO-EXTENDED'));
    });
}

/**
 * Loads using AJAX the next page for a data set
 * @param {String} action the action to perform, it needs to be a function in the data set
 * @param {String} [handler] the function to perform
 * @param {String} [id] the model identifier
 */
DataSet.performAction = function (action, handler, id) {
    if (id) DataSet.id = id;
    if (action) {
        if ($.isFunction(DataSet[action])) {
            console.info("Invoke action '" + action + "'");
            DataSet[action].apply(this, arguments);
        } else {
            console.error("There is no action with name '" + action + "' registered");
        }
    }
}

/**
 * Views the current model.
 */
DataSet.view = function () {
    DataSet.checkIdentifier();
    $.get(REQUEST_PATH + "/" + DataSet.id + "/view", function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Adds the current model.
 */
DataSet.add = function () {
    $.get(REQUEST_PATH + "/add", function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Edit the current model.
 */
DataSet.edit = function () {
    DataSet.checkIdentifier();
    $.get(REQUEST_PATH + "/" + DataSet.id + "/edit", function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Delete the current model.
 */
DataSet.delete = function () {
    DataSet.checkIdentifier();
    $.get(REQUEST_PATH + "/" + DataSet.id + "/delete", function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Prints the current model.
 */
DataSet.print = function () {
    $.get(REQUEST_PATH + "/" + DataSet.id + "/add", function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Refresh the current view.
 */
DataSet.refresh = function () {
    DataSet.open({});
}

/**
 * Opens an upload dialog and lets the user upload a file to add a new model.
 */
DataSet.upload = function () {
    $("div.dataset-drop-zone").click();
}

/**
 * Downloads a model.
 */
DataSet.download = function () {
    let uri = DataSet.uri({}, DataSet.id + "/download");
    $("#dataset-download").attr("src", uri);
}

/**
 * Shows an HTML fragment which contains a data set modal.
 * @param {String} html the modal
 */
DataSet.loadModal = function (html) {
    console.log(html);
    $('#dataset-modal').remove();
    $(document.body).append(html);
    let modal = new bootstrap.Modal('#dataset-modal', {});
    modal.show();
    DataSet.registerModal(modal);
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
 * Closes last dialog.
 */
DataSet.closeModal = function () {
    let modal = DataSet.getModals().pop();
    if (modal) {
        modal.hide();
    }
}

/**
 * Registers a modal, which is used to validate if the user clicks outside the popup.
 * @param {bootstrap.Modal} modal the modal instance
 */
DataSet.registerModal = function (modal) {
    DataSet.getModals().push(modal);
}

/**
 * Returns the registers modals.
 *
 * @return {bootstrap.Modal[]} the modals.
 */
DataSet.getModals = function () {
    DataSet.modals = DataSet.modals || [];
    return DataSet.modals;
}

/**
 * Saves the current data set model
 */
DataSet.save = function () {
    DataSet.closeModal();
}

/**
 * Shows an informational message.
 * @param {String} title the title
 * @param {String} message the message to display, it can contain HTML tags
 */
DataSet.showInfoAlert = function (title, message) {
    DataSet.showAlert(title, message, DATASET_ALERT_TYPE_INFO);
}

/**
 * Shows a warning message.
 * @param {String} title the title
 * @param {String} message the message to display, it can contain HTML tags
 */
DataSet.showWarnAlert = function (title, message) {
    DataSet.showAlert(title, message, DATASET_ALERT_TYPE_WARN);
}

/**
 * Shows an error message.
 * @param {String} title the title
 * @param {String} message the message to display, it can contain HTML tags
 */
DataSet.showErrorAlert = function (title, message) {
    DataSet.showAlert(title, message, DATASET_ALERT_TYPE_ERROR);
}

/**
 * Shows an alert
 * @param {String} title the title
 * @param {String} message the message to display, it can contain HTML tags
 * @param {String} type the type of alert
 */
DataSet.showAlert = function (title, message, type) {
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
 * Validates whether a model identifier is set.
 */
DataSet.checkIdentifier = function () {
    if (!DataSet.id) throw new Error("An identifier for the current model is not registered");
}

/**
 * Initializes various notifications related components.
 */
DataSet.initNotifications = function () {
    let text = $("#dataset-message").text();
    if (!$.isEmptyObject(text)) {
        DataSet.showErrorAlert("Query", text);
    }
}

/**
 * Returns whether the data set has a date/time filter.
 * @return {Boolean} true if there is a date/time filter, false otherwie
 */
DataSet.hasTimeFilter = function () {
    return $("#daterange").length;
}

/**
 * Returns the time range filter.
 *
 * If there is no time filter, an empty range is returned.
 *
 * @return {Object[]} the start and end of the time range.
 */
DataSet.getTimeFilter = function () {
    if (DataSet.hasTimeFilter()) {
        let data = $('#daterange').data('daterangepicker');
        let range = [data.startDate, data.endDate];
        return range;
    } else {
        return [];
    }
}

/**
 * Initializes various fields related to data sets.
 */
DataSet.initFields = function () {
    if (!DataSet.hasTimeFilter()) return;
    let range = DataSet.getQueryParam('range');
    if ($.isEmptyObject(range)) range = $('#daterange span.d-none').val();
    let startDate = moment().startOf('day');
    let endDate = moment().endOf('day');
    if (!$.isEmptyObject(range)) {
        console.log("Range: " + range);
        let startEndRange = range.split(DATE_RANGE_SEPARATOR);
        startDate = moment(startEndRange[0]);
        endDate = moment(startEndRange.length === 2 ? startEndRange[1] : startDate);
    }
    console.log("Range: " + startDate + ", " + endDate);
    let formatter = function (start, end) {
        $('#daterange span').html(start.format('L') + ' - ' + end.format('L'));
    };
    $('#daterange').daterangepicker({
        startDate: startDate,
        endDate: endDate,
        opens: 'left',
        drops: 'down',
        timePicker: true,
        autoApply: true,
        ranges: {
            'Today': [moment().startOf('day'), moment().endOf('day')],
            'Yesterday': [moment().subtract(1, 'days').startOf('day'), moment().subtract(1, 'days').endOf('day')],
            'Last 7 Days': [moment().subtract(6, 'days').startOf('day'), moment().endOf('day')],
            'Last 30 Days': [moment().subtract(29, 'days').startOf('day'), moment().endOf('day')],
            'This Month': [moment().startOf('month').startOf('day'), moment().endOf('month').endOf('day')],
            'Last Month': [moment().subtract(1, 'month').startOf('day').startOf('month'), moment().subtract(1, 'month').endOf('month').endOf('day')]
        }
    }, function (start, end, label) {
        formatter(start, end);
        DataSet.reload();
    });
    formatter(startDate, endDate);
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
 * Transforms a plain tables into a resizable ones.
 */
DataSet.initTables = function () {
    $(".dataset-table th")
        .css({
            position: "relative"
        })
        .prepend("<div class='dataset-table-resizer'></div>")
        .resizable({
            resizeHeight: false,
            handleSelector: "",
            onDragStart: function (e, $el, opt) {
                return $(e.target).hasClass("resizer");
            }
        });
}

/**
 * Initializes the JS library which handles the file upload.
 */
DataSet.initUpload = function () {
    let dropZone = new Dropzone("div." + DATASET_DROP_ZONE_CLASS, {
        //autoProcessQueue: false,
        createImageThumbnails: false,
        disablePreviews: true,
        url: REQUEST_PATH + "/upload"
    });
    dropZone.on("success", function (file) {
        dropZone.removeFile(file);
        DataSet.showInfoAlert("Upload", "File  '" + file.name + "' was uploaded successfully");
    });
    dropZone.on("error", function (file) {
        DataSet.showInfoAlert("Upload", "Failed to upload file '" + file.name + "'");
    });
    dropZone.on("processing", function (file) {
        // DataSet.showInfoAlert("Upload", "File '" + file.name + "' will be uploaded to the server");
    });
    dropZone.on("complete", function (file) {
        // DataSet.showInfoAlert("Upload", "File '" + file.name + "' done'");
    });
    DataSet.dropZone = dropZone;
}

/**
 * Initializes the data set.
 */
DataSet.init = function () {
    DataSet.initEvents();
    DataSet.initFields();
    DataSet.initNotifications();
    DataSet.initTables();
    DataSet.initUpload();
}