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
 * Returns whether the current page has a data set.
 */
DataSet.exists = function () {
    return $("#dataset-download").length > 0;
}

/**
 * Returns an URI to execute a data set request.
 *
 * @param {String} [path] an optional path to add to the base URI
 * @param {Object} params the new parameters
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.self=true] an optional boolean, to calculate the URI to the current page (within the same resource)
 * @param {Boolean} [options.params=false] an optional boolean, to include the parameters in the URI
 */
DataSet.getUri = function (path, params, options) {
    params = this.getParams(params, options);
    return Application.getUri(path, params, options);
}

/**
 * Returns all the parameters required for a data set data request.
 *
 * @param {Object} params the new parameters
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @param {Boolean} [options.params=false] an optional boolean, to include the filter parameters
 */
DataSet.getParams = function (params, options) {
    options = options || APP_AJAX_DEFAULT_OPTIONS;
    options.params = Utils.isDefined(options.params) ? options.params : false;
    params = params || {};
    if (options.params) {
        params["query"] = $("#search").val();
        let timeFilter = this.getTimeFilter();
        if (timeFilter.length > 0) {
            params["range"] = timeFilter[0].toISOString() + DATE_RANGE_SEPARATOR + timeFilter[1].toISOString();
        }
    }
    return params;
}

/**
 * Opens a data set page, using the current request parameters with new parameters
 *
 * @param {String} [path] the path to add to the base URI
 * @param {Object} params the parameter overrides
 */
DataSet.open = function (path, params) {
    params = this.getParams(params, {params: true});
    Application.openSelf(path, params);
}

/**
 * Reloads the data set page.
 */
DataSet.reload = function () {
    this.open("", {});
}

/**
 * Executes a GET request, which includes the parameters of the current requests
 *
 * @param {String} path the path
 * @param {Object} params the parameter overrides
 * @param {Function} callback the callback to be called with the response
 * @param {Object} [options] the callback to be called with the response
 */
DataSet.get = function (path, params, callback, options) {
    params = this.getParams(params, {params: false});
    options = options || {};
    options.params = true;
    Application.get(path, params, callback, options)
}

/**
 * Triggers a query in the data set.
 * @param {String } query the query to execute, if empty
 */
DataSet.search = function (query) {
    if (Utils.isNotEmpty(query)) $("#search").val(query);
    this.open("");
    return false;
}

/**
 * Loads using AJAX the next page for a data set
 * @param {Integer|String} page the page number, first one starts at 1
 */
DataSet.loadPage = function (page) {
    page = parseInt(page);
    DataSet.get("page", {page: page}, function (data, status, xhr) {
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
    Logger.info("View record '" + id + "'");
    DataSet.updateId(id);
    Application.get(DataSet.getId() + "/view", {}, function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Adds the current model.
 */
DataSet.add = function () {
    Logger.info("Add a new record");
    Application.get("add", {}, function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Edit the current model.
 */
DataSet.edit = function (id) {
    Logger.info("Edit record '" + id + "'");
    DataSet.updateId(id);
    Application.get(DataSet.getId() + "/edit", {}, function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Delete the current model.
 */
DataSet.delete = function (id) {
    Logger.info("Delete record '" + id + "'");
    DataSet.updateId(id);
    Application.delete(DataSet.getId() + "/delete", {}, function (json) {
        if (json.success) {
            DataSet.refresh();
        } else {
            Application.showErrorAlert("Delete", json.message);
        }
    }, {dataType: "json"});
}

/**
 * Prints the current model.
 */
DataSet.print = function (id) {
    DataSet.updateId(id);
    $.get(APP_REQUEST_PATH + "/" + DataSet.getId() + "/add", function (data) {
        DataSet.loadModal(data);
    });
}

/**
 * Refresh the current view.
 */
DataSet.refresh = function () {
    DataSet.search('');
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
    Application.loadModal("dataset-modal", html);
}

/**
 * Shows the actions dropdown.
 *
 * @param {EventSource} event the click event
 * @param {String} id the model identifier
 */
DataSet.showActions = function (event, id) {
    event.stopPropagation();
    Application.closePopups();
    this.id = id;
    let actions = $('#dataset-actions');
    let element = $(event.target);
    if (this.popper) this.popper.destroy();
    this.popper = Popper.createPopper(element[0], actions[0], {
        placement: 'bottom'
    });
    this.popper.update();
    Application.registerPopup(actions);
    actions.show();
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters
 * @param {String}field the new parameters
 * @param {String}direction the new parameters
 */
DataSet.sort = function (field, direction) {
    let requestParams = $.extend({}, APP_REQUEST_QUERY);
    let sort = field + "=" + direction;
    requestParams["sort"] = sort;
    this.open("", requestParams);
}

/**
 * Saves the current data set model
 */
DataSet.save = function () {
    let me = DataSet;
    let path = Utils.isEmpty(me.id) ? "" : me.id;
    let url = DataSet.getUri(path, {}, {params: false});
    let closeModel = false;
    let form = $('#dataset-form').ajaxSubmit({
        url: url,
        type: 'POST',
        dataType: 'json',
        beforeSubmit: function (data, form, options) {
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
                for (let field in errors) {
                    let message = errors[field];
                    let formField = $("#dataset-form input[name='" + field + "']");
                    formField.addClass("is-invalid");
                    Application.showTooltip(formField, message);
                }
            }
        }
    });
    if (closeModel) Application.closeModal();
}

/**
 * Updates for data before it is sent to the server
 * @param {Array} data an array with object, one property "name" with the value
 */
DataSet.updateFormFields = function (data) {
    let fieldNames = {};
    for (let tuple of data) {
        fieldNames[tuple["name"]] = true;
    }
    $('#dataset-form input').each(function (index) {
        if ($(this).attr("type") === "checkbox") {
            let name = $(this).attr("name");
            if (!fieldNames[name]) {
                data.push({name: name, value: 'off'});
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
    Logger.debug("Data Set Range: " + this.formatRange(startDate, endDate));
    let formatter = function (start, end) {
        $('#daterange span').html(DataSet.formatRange(start, end));
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
            'Last 5 Minutes': [moment().subtract(5, 'minutes'), moment().endOf('day')],
            'Last 15 Minutes': [moment().subtract(15, 'minutes'), moment().endOf('day')],
            'Last 30 Minutes': [moment().subtract(30, 'minutes'), moment().endOf('day')],
            'Last 1 Hour': [moment().subtract(1, 'hours'), moment().endOf('day')],
            'Last 4 Hours': [moment().subtract(4, 'hours'), moment().endOf('day')],
            'Last 8 Hours': [moment().subtract(8, 'hours'), moment().endOf('day')],
            'Last 2 Days': [moment().subtract(1, 'days').startOf('day'), moment().endOf('day')],
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
 * Formats a time range.
 * @param {Object} start the start time as an instance, JS Date or Moment object
 * @param {Object} end the end time as an instance, JS Date or Moment object
 * @return {String} the formatted range as a string
 */
DataSet.formatRange = function (start, end) {
    start = moment(start);
    end = moment(end);
    let localStart = start.clone();
    let adjustedEnd = moment();
    let diff = adjustedEnd.diff(localStart, 'minutes');
    let unit = "m";
    if (diff > 300) {
        localStart.subtract(30, "minutes");
        if (diff > 1400) adjustedEnd = end;
        diff = adjustedEnd.diff(localStart, 'hours');
        unit = "h";
        if (diff > 72) {
            diff = adjustedEnd.diff(localStart, 'days');
            unit = "d";
        }
    }
    return start.format('L') + ' - ' + end.format('L') + " (" + diff + unit + ")";
}

/**
 * Initializes data set specific actions.
 */
DataSet.initActions = function () {
    Application.bind("dataset.view", DataSet.view);
    Application.bind("dataset.add", DataSet.add);
    Application.bind("dataset.edit", DataSet.edit);
    Application.bind("dataset.delete", DataSet.delete);
    Application.bind("dataset.refresh", DataSet.refresh);
    Application.bind("dataset.download", DataSet.download);
    Application.bind("dataset.upload", DataSet.upload);
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
    $(".dataset-table").click(function (event) {
        let target = $(event.target);
        let parent = target.parent();
        if (target.get(0).tagName === "SPAN" && parent.get(0).tagName === "TD" && parent.hasClass("filterable")) {
            let text = target.text();
            target = parent;
            let field = null;
            let tdIndex = target.index();
            $('.dataset-table tr th').each(function (index) {
                if ($(this).attr("field_index") == tdIndex) {
                    field = $(this).attr('field_name');
                    return false;
                }
            });
            Logger.debug("Click on '" + text + "', index " + tdIndex + ", field " + field);
            if (Utils.isNotEmpty(text) && Utils.isNotEmpty(field)) {
                let currentQuery = field + DATASET_FILTERABLE_OPERATOR + DATASET_FILTERABLE_QUOTE_CHAR + text + DATASET_FILTERABLE_QUOTE_CHAR;
                let previousQuery = Application.getQueryParam("query");
                if (Utils.isNotEmpty(previousQuery)) currentQuery += " AND " + previousQuery;
                DataSet.search(currentQuery);
            }
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
        url: APP_REQUEST_PATH + "/upload"
    });
    dropZone.on("success", function (file) {
        dropZone.removeFile(file);
        Application.showInfoAlert("Upload", "File  '" + file.name + "' was uploaded successfully");
    });
    dropZone.on("error", function (file) {
        Application.showErrorAlert("Upload", "Failed to upload file '" + file.name + "'");
    });
    dropZone.on("processing", function (file) {
        Application.showInfoAlert("Upload", "File '" + file.name + "' will be uploaded to the server");
    });
    dropZone.on("complete", function (file) {
        // DataSet.showInfoAlert("Upload", "File '" + file.name + "' done'");
    });
    DataSet.dropZone = dropZone;
}

/**
 * Initializes the view based on URI (hash) parameters.
 */
DataSet.initFromUri = function () {
    let path = Application.getHashPath();
    if (path.startsWith("/view")) {
        let parts = path.split("/");
        if (parts.length > 2) DataSet.view(parts[2]);
    }
}

/**
 * Initializes the data set.
 */
DataSet.initialize = function () {
    Logger.debug("Initialize data set");
    this.initActions();
    this.initFields();
    this.initNotifications();
    this.initTables();
    this.initUpload();
    this.initFromUri();
}

// Initialize the data set
DataSet.initialize();