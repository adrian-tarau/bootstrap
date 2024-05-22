/*
* The Chart Global Variables
 */
window.Chart = window.Chart || {};
window.Chart.Tooltip = window.Chart.Tooltip || {};

/**
 * Loads a chart.
 *
 * @param {String} id the chart identifier
 * @param {Object} options the chart options
 */
Chart.load = function (id) {
    let selector = "#" + id;
    Logger.debug("Load chart '" + id + "'");
    $(selector).LoadingOverlay("show");
    Application.get("/chart/render/" + id, {}, function (data) {
        Chart.render("#" + id, data);
    }, {
        self: false, dataType: "json", mask: selector
    });
}

/**
 * Renders a chart.
 *
 * @param {String} selector the DOM selector
 * @param {Object} options the chart options
 */
Chart.render = function (selector, options) {
    options = Utils.intercept(options, function (value) {
        if (value.match(/Chart\.(.*)/g) != null || value.match(/DataSet\.(.*)/g)) {
            return eval("(" + value + ")");
        } else {
            return value;
        }
    });
    Logger.debug("Render chart '" + selector + "', options " + Utils.toString(options));
    let chart = new ApexCharts(document.querySelector(selector), options);
    chart.render();
}

/**
 * Formats the tooltip to have no
 * @param seriesName
 * @return {string} the title
 */
Chart.Tooltip.formatNoTitle = function (seriesName) {
    return '';
}

/**
 * Formats the tooltip to have no
 * @param {object} value the value to format
 * @return {string}
 */
Chart.Tooltip.formatTimestamp = function (value) {
    if (Utils.isNumber(value)) {
        return moment(value).format('L LTS');
    } else {
        return value;
    }
}
