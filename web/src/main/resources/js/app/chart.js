const CHART_RENDERED_ATTR = "chart_rendered";
/*
* The Chart Global Variables
 */
window.Chart = window.Chart || {};
window.Chart.Tooltip = window.Chart.Tooltip || {};

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
        if (!Utils.isDefined(renderedAttr)) {
            graphElement.attr(CHART_RENDERED_ATTR, true);
            Logger.debug("Load chart '" + id + "'");
            graphElement.LoadingOverlay("show");
            Application.get("/chart/render/" + id, {}, function (data) {
                Chart.render("#" + id, data);
            }, {
                self: false, dataType: "json", mask: selector
            });
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
 * Process any unprocessed chart elements and render them.
 *
 * If there is no parent passed to the function, the whole page will be searched for charts.
 *
 * @param {String|Element} [parent] the element where to search for charts.
 */
Chart.process = function (parent) {
    Logger.info("Process charts in " + parent);
    $(".chart-container").each(function (index, element) {
        let renderedAttr = $(element).attr(CHART_RENDERED_ATTR);
        if (!Utils.isDefined(renderedAttr)) Chart.load(element.id);
    });
}

Application.bind("start", Chart.process);
