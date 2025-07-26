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
 * Clears the search field and executes the query again.
 */
Application.Search.clear = function () {
    $('#search').val('');
    Application.Search.query('');
}

/**
 * Executes a search with a given query.
 *
 * If the current page is a data set, the expression is processed by a data set, otherwise it goes to the
 * global search engine (Apache Lucene), unless it is explicitly asked to be the global engine.
 *
 * @param {String} text the query text
 * @param {Boolean} [global] true to use the global search engine
 * @param {Boolean} [newWindow=false] an optional flag, true to control if the URI is opened in a new window, false otherwise
 */
Application.Search.query = function (text, global, newWindow) {
    this.grow();
    global = Utils.defaultIfNotDefinedOrNull(global, false);
    if (DataSet.exists() && !global) {
        DataSet.search(text);
    } else {
        if (Utils.isEmpty(text)) text = $("#search").val();
        let params = {
            query: text
        }
        Application.open("/search", params, newWindow);
    }
}

/**
 * Executes a search with a given query.
 *
 * If the current page is a data set, the expression is processed by a data set, otherwise it goes to the
 * global search engine (Apache Lucene), unless it is explicitly asked to be the global engine.
 *
 * @param {String} field the field name
 * @param {String} text the field value
 * @param {Boolean} [global] true to use the global search engine
 * @param {Boolean} [newWindow=false] an optional flag, true to control if the URI is opened in a new window, false otherwise
 */
Application.Search.queryField = function (field, text, global, newWindow) {
    let query;
    if (global) {
        query = field + SEARCH_ENGINE_FILTERABLE_OPERATOR + SEARCH_ENGINE_FILTERABLE_QUOTE_CHAR + text + SEARCH_ENGINE_FILTERABLE_QUOTE_CHAR;
    } else {
        query = field + DATASET_FILTERABLE_OPERATOR + DATASET_FILTERABLE_QUOTE_CHAR + text + DATASET_FILTERABLE_QUOTE_CHAR;
    }
    Application.Search.query(query, global, newWindow);
}

/**
 * Extracts the current query and appends another query.
 *
 * @param {String} text the new query text passed
 * @param {Boolean} [global] true to use the global search engine
 * @param {Boolean} [newWindow=false] an optional flag, true to control if the URI is opened in a new window, false otherwise
 */
Application.Search.join = function (text, global, newWindow) {
    if (Utils.isEmpty(text)) {
        Application.Search.query("", global, newWindow);
    } else {
        let currentText = Application.getQueryParam("query");
        if (Utils.isNotEmpty(currentText)) text += " AND " + currentText;
        Application.Search.query(text, global, newWindow);
    }
}

/**
 * Extracts the current query and appends another query.
 *
 * @param {String} field the field name
 * @param {String} text the field value
 * @param {Boolean} [global] true to use the global search engine
 * @param {Boolean} [newWindow=false] an optional flag, true to control if the URI is opened in a new window, false otherwise
 */
Application.Search.joinField = function (field, text, global, newWindow) {
    let query;
    if (global) {
        query = field + SEARCH_ENGINE_FILTERABLE_OPERATOR + SEARCH_ENGINE_FILTERABLE_QUOTE_CHAR + text + SEARCH_ENGINE_FILTERABLE_QUOTE_CHAR;
    } else {
        query = field + DATASET_FILTERABLE_OPERATOR + DATASET_FILTERABLE_QUOTE_CHAR + text + DATASET_FILTERABLE_QUOTE_CHAR;
    }
    Application.Search.join(query, global, newWindow);
}

/**
 * Grows the search field as needed.
 */
Application.Search.grow = function () {
    $('#search').trigger('autogrow');
}

/**
 * Initializes search related functions.
 */
Application.Search.init = function () {
    $('#search').inputAutogrow({maxWidth: 800, minWidth: 400, trailingSpace: 10});
    this.grow();
}

// Bind events
Application.bind("search", Application.Search.query);
Application.Search.init();