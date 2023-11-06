/*
* The Data Set Global Variables
 */
window.DataSet = window.DataSet || {};

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
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.self=true] an optional boolean, to calculate the URI to the current page (self)
 * @param {Boolean} [options.params=true] an optional boolean, to include the parameters in the URI
 */
DataSet.getUri = function (params, path, options) {
    params = this.getQuery(params, options);
    return Application.getUri(params, path, options);
}

/**
 * Takes a collection of parameters and creates an object with all query parameters.
 *
 * @param {Object} params the new parameters
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.self=true] an optional boolean, to calculate the URI to the current page (self)
 * @param {Boolean} [options.params=true] an optional boolean, to include the parameters in the URI
 */
DataSet.getQuery = function (params, options) {
    options = options || {};
    options.self = (typeof options.self === 'undefined') ? true : options.self;
    options.params = (typeof options.params === 'undefined') ? true : options.params;
    params = params || {};
    if (options.params) {
        params["query"] = $("#query").val();
        let timeFilter = this.getTimeFilter();
        if (timeFilter.length > 0) {
            params["range"] = timeFilter[0].toISOString() + DATE_RANGE_SEPARATOR + timeFilter[1].toISOString();
        }
    }
    return params;
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters.
 *
 * @param {Object} params the new parameters
 * @param {String} [path] an optional path to add to the base URI
 */
DataSet.open = function (params, path) {
    params = this.getQuery(params);
    Application.openSelf(params, path);
}

/**
 * Reloads the data set page.
 */
DataSet.reload = function () {
    this.open({});
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters.
 *
 * @param {String} path the path
 * @param {Object} params the new parameters
 * @param {Function} callback the callback to be called with the response
 */
DataSet.ajax = function (path, params, callback) {
    params = this.getQuery(params);
    Application.ajax(path, params, callback, true)
}

/**
 * Triggers a query in the data set.
 * @param {String } query the query to execute, if empty
 */
DataSet.search = function (query) {
    if (Utils.isNotEmpty(query)) $("#query").val(query);
    this.open("");
    return false;
}

/**
 * Loads using AJAX the next page for a data set
 * @param {Integer|String} page the page number, first one starts at 1
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
 * Views the current model.
 */
DataSet.view = function (id) {
    DataSet.updateId(id);
    $.get(REQUEST_PATH + "/" + DataSet.getId() + "/view", function (data) {
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
DataSet.edit = function (id) {
    DataSet.updateId(id);
    $.get(REQUEST_PATH + "/" + DataSet.getId() + "/edit", function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Delete the current model.
 */
DataSet.delete = function (id) {
    DataSet.updateId(id);
    $.ajax({
        url: REQUEST_PATH + "/" + DataSet.getId() + "/delete",
        dataType: "json",
        type: 'DELETE',
        success: function (json) {
            if (json.success) {
                DataSet.refresh();
            } else {
                Application.showErrorAlert("Delete", json.message);
            }
        }
    })
}

/**
 * Prints the current model.
 */
DataSet.print = function (id) {
    DataSet.updateId(id);
    $.get(REQUEST_PATH + "/" + DataSet.getId() + "/add", function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Refresh the current view.
 */
DataSet.refresh = function () {
    this.open({});
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
    let uri = this.getUri({}, DataSet.getId() + "/download");
    $("#dataset-download").attr("src", uri);
}

/**
 * Shows an HTML fragment which contains a data set modal.
 * @param {String} html the modal
 */
DataSet.loadModal = function (html) {
    Logger.debug(html);
    $('#dataset-modal').remove();
    $(document.body).append(html);
    let modal = new bootstrap.Modal('#dataset-modal', {});
    modal.show();
    this.registerModal(modal);
}

/**
 * Shows the actions dropdown.
 *
 * @param {EventSource} event the click event
 * @param {String} id the model identifier
 */
DataSet.showActions = function (event, id) {
    event.stopPropagation();
    this.closePopups();
    this.id = id;
    let actions = $('#dataset-actions');
    let element = $(event.target);
    if (this.popper) {
        this.popper.destroy();
    }
    this.popper = Popper.createPopper(element[0], actions[0], {
        placement: 'bottom'
    });
    this.popper.update();
    this.registerPopup(actions);
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
    this.open(requestParams);
}


/**
 * Closes all open popups.
 */
DataSet.closePopups = function () {
    this.getPopups().forEach(function (popup) {
        popup.hide();
    });
    this.popups = [];
}

/**
 * Registers a popup, which is used to validate if the user clicks outside the popup.
 * @param {Object} element a
 */
DataSet.registerPopup = function (element) {
    this.getPopups().push(element);
}

/**
 * Returns the registers popups.
 *
 * @return {Object[]} the popups.
 */
DataSet.getPopups = function () {
    this.popups = this.popups || [];
    return DataSet.popups;
}

/**
 * Closes last dialog.
 */
DataSet.closeModal = function () {
    let modal = this.getModals().pop();
    if (modal) {
        modal.hide();
    }
}

/**
 * Registers a modal, which is used to validate if the user clicks outside the popup.
 * @param {bootstrap.Modal} modal the modal instance
 */
DataSet.registerModal = function (modal) {
    this.getModals().push(modal);
}

/**
 * Returns the registers modals.
 *
 * @return {bootstrap.Modal[]} the modals.
 */
DataSet.getModals = function () {
    this.modals = this.modals || [];
    return this.modals;
}

/**
 * Saves the current data set model
 */
DataSet.save = function () {
    let me = DataSet;
    let url = Utils.isEmpty(me.id) ? DataSet.getUri({}, "", {params: false}) : DataSet.getUri({}, me.id, {params: false});
    let closeModel = false;
    let form = $('#dataset-form').ajaxSubmit({
        url: url,
        type: 'POST',
        dataType: 'json',
        beforeSubmit: function(data, form, options) {
            DataSet.updateFormFields(data);
            Logger.info("Before form submission, response " + Utils.toString(data));
            // form data array is an array of objects with name and value properties
            // [ { name: 'username', value: 'jresig' }, { name: 'password', value: 'secret' } ]
        },
        error: function (jqXHR, textStatus, errorThrown) {
            Logger.error("An error was encountered which submitting the form to " + url + ", error: " + errorThrown);
        },
        success: function (data, textStatus, jqXHR) {
            Logger.info("Form was submitted successfully, response " + Utils.toString(data));
            if (data.success) {
                me.reload();
            } else {
                $('#dataset-form input').removeClass('is-invalid').tooltip("dispose");
                let errors = data.errors || {};
                Application.showErrorAlert("Validation", "Form cannot be submitted with invalid values");
                for(let field in errors) {
                    let message = errors[field];
                    let formField = $("#dataset-form input[name='"+field+"']");
                    formField.addClass("is-invalid");
                    Application.showTooltip(formField, message);
                }
            }
        }
    });
    if (closeModel) this.closeModal();
}

/**
 * Updates for data before it is sent to the server
 * @param {Array} data an array with object, one property "name" with the value
 */
DataSet.updateFormFields = function (data) {
    let fieldNames = {};
    for(let tuple of data) {
        fieldNames[tuple["name"]] = true;
    }
    $('#dataset-form input').each(function(index){
        if ($(this).attr("type") === "checkbox") {
            let name = $(this).attr("name");
            if (!fieldNames[name]) {
                data.push({name : name, value : 'off'});
            }
        }
    });
}

/**
 * Return the current model identifier.
 */
DataSet.getId = function () {
    if (Utils.isEmpty(this.id)) throw new Error("A model identifier is not provided");
    return this.id;
}

/**
 * Validates and updated a model identifier.
 */
DataSet.updateId = function (id) {
    if (Utils.isNotEmpty(id)) this.id = id;
}

/**
 * Initializes various notifications related components.
 */
DataSet.initNotifications = function () {
    let text = $("#dataset-message").text();
    if (Utils.isNotEmpty(text)) Application.showErrorAlert("Query", text);
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
    if (this.hasTimeFilter()) {
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
    if (!this.hasTimeFilter()) return;
    let range = Application.getQueryParam('range');
    if (Utils.isEmpty(range)) range = $('#daterange span.d-none').val();
    let startDate = moment().startOf('day');
    let endDate = moment().endOf('day');
    if (Utils.isNotEmpty(range)) {
        Logger.debug("Data Set Range Query: " + range);
        let startEndRange = range.split(DATE_RANGE_SEPARATOR);
        startDate = moment(startEndRange[0]);
        endDate = moment(startEndRange.length === 2 ? startEndRange[1] : startDate);
    }
    Logger.debug("Data Set Range: " + startDate + ", " + endDate);
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
 * Initializes data set specific actions.
 */
DataSet.initActions = function () {
    Application.bind("dataset.view", DataSet.view);
    Application.bind("dataset.add", DataSet.add);
    Application.bind("dataset.edit", DataSet.edit);
    Application.bind("dataset.delete", DataSet.delete);
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
            handleSelector: "",
            handles: 'e',
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
DataSet.initialize = function () {
    Logger.debug("Initialize data set");
    this.initActions();
    this.initEvents();
    this.initFields();
    this.initNotifications();
    this.initTables();
    this.initUpload();
}

// Initialize the data set
DataSet.initialize();