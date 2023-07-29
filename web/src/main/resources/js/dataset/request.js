/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters
 * @param {Object }params the new parameters
 */
function openDataSet(params) {
    let requestParams = $.extend({}, REQUEST_QUERY);
    requestParams = $.extend(requestParams, params);
    let uri = REQUEST_PATH + "?" + $.param(requestParams);
    console.info("Open dat set " + uri);
    window.location.href = uri;
}

/**
 * Takes a collection of parameters and queries the same end points with original parameters
 * plus the additional parameters
 * @param {String}field the new parameters
 * @param {String}direction the new parameters
 */
function sortDataSet(field, direction) {
    let requestParams = $.extend({}, REQUEST_QUERY);
    let sort = field + "=" + direction;
    requestParams["sort"] = sort;
    openDataSet(requestParams);
}