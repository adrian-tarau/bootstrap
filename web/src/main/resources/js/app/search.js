/*
* The Search Global Variables
 */
window.Application.Search = window.Application.Search || {};

/**
 * Executes a search by extracting the search text from object was clicked on.
 * @param element the element was clicked on
 */
Application.Search.click = function (element) {
    let text = ($(element).text());
    Application.Search.query(text);
}

/**
 * Executes a search with a given query.
 *
 * @param {String} text the query text passed to the search engine (Apache Lucene syntax)
 */
Application.Search.query = function (text) {
    if (DataSet.exists()) {
        DataSet.search(text);
    } else {
        if (Utils.isEmpty(text)) text = $("#search").val();
        let params = {
            query: text
        }
        Application.open("/search", params);
    }
}

/**
 * Extracts the current query and appends another query.
 *
 * @param {String} text the new query text passed to the search
 */
Application.Search.join = function (text) {
    if (Utils.isEmpty(text)) {
        Application.Search.query("");
    } else {
        let currentText = Application.getQueryParam("query");
        if (Utils.isNotEmpty(currentText)) text += " AND " + currentText;
        Application.Search.query(text);
    }
}

/**
 * Extracts the current query and appends another query.
 *
 * @param {String} field the field name
 * @param {String} text the field value
 */
Application.Search.joinField = function (field, text) {
    Application.Search.join(field + DATASET_FILTERABLE_OPERATOR + DATASET_FILTERABLE_QUOTE_CHAR + text + DATASET_FILTERABLE_QUOTE_CHAR);
}

// Bind events
Application.bind("search", Application.Search.query);