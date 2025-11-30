const CHART_RENDERED_ATTR = "chart-rendered";
const CHART_DATA_ATTR = "chart-data";
/*
* The Chart Global Variables
 */
window.Chart = window.Chart || {};
window.Chart.Tooltip = window.Chart.Tooltip || {};
window.Chart.Color = window.Chart.Color || {};

/**
 * Loads a chart.
 *
 * @param {String} id the chart identifier
 * @param {Object} [options] the chart options
 */
Chart.load = function (id, options) {
    let selector = "#" + id;
    let graphElement = $(selector);
    if (graphElement.length > 0) {
        let renderedAttr = graphElement.attr(CHART_RENDERED_ATTR);
        let chartDataAttr = graphElement.attr(CHART_DATA_ATTR);
        if (!Utils.isDefined(renderedAttr)) {
            graphElement.attr(CHART_RENDERED_ATTR, true);
            if (Utils.isDefined(chartDataAttr)) {
                let data = JSON.parse(atob(chartDataAttr));
                Utils.defer(function () {
                    Chart.render("#" + id, data);
                });
            } else {
                //Logger.debug("Load chart '" + id + "'");
                graphElement.LoadingOverlay("show");
                Application.get("/chart/render/" + id, {}, function (data) {
                    Chart.render("#" + id, data);
                }, {
                    self: false, dataType: "json", mask: selector
                });
            }
        }
    } else {
        Logger.error("A chart with identifier '" + id + "' does not exist");
    }
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
    //Logger.debug("Render chart '" + selector + "', options " + Utils.toString(options));
    let chart = new ApexCharts(document.querySelector(selector), options);
    chart.render();
}

/**
 * Process any unprocessed chart elements and render them.
 *
 * If there is no parent passed to the function, the whole page will be searched for charts.
 *
 * @param {String|Element} [parent] the element where to search for charts.
 */
Chart.process = function (parent) {
    Logger.debug("Process charts in " + (parent? "'" + parent + "'" : "ROOT"));
    $(".chart-container").each(function (index, element) {
        let renderedAttr = $(element).attr(CHART_RENDERED_ATTR);
        if (!Utils.isDefined(renderedAttr)) Chart.load(element.id);
    });
}

/**
 * Formats the tooltip to have no title.
 * @param seriesName
 * @return {string} the title
 */
Chart.Tooltip.formatNoTitle = function (seriesName) {
    return '';
}

/**
 * Formats the tooltip as a timestamp.
 *
 * @param {object} value the value to format
 * @return {string} the timestamp as a string
 */
Chart.Tooltip.formatTimestamp = function (value) {
    if (Utils.isNumber(value)) {
        return moment(value).format('L LTS');
    } else {
        return value;
    }
}

/**
 * Formats the tooltip as a duration.
 *
 * @param {object} value the value to format
 * @return {string} the timestamp as a string
 */
Chart.Tooltip.formatDuration = function (value) {
    if (Utils.isNumber(value)) {
        return Utils.formatMillis(value);
    } else {
        return value;
    }
}

/**
 * Returns the color of a chart element based on the value.
 * @param {number} value the value of the current element
 * @param {int} seriesIndex the series index
 * @param {chart} w
 * @return {string} the color
 */
Chart.Color.negativeValues = function ({value, seriesIndex, w}) {
    if (value < 0) {
        return '#FF4560'
    } else {
        return '#008FFB'
    }
}

Application.bind("start", Chart.process);
