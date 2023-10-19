/*
* The Search Global Variables
 */
window.Application.Search = window.Application.Search || {};

/**
 * Executes a search with a given query.
 *
 * @param {String} text the query text passed to the search engine (Apache Lucene syntax)
 */
Application.Search.query = function (text) {
    if (Utils.isEmpty(text)) text = $("#search").val();
    let params = {
        query: text
    }
    Application.open(params, "/search")
}

/**
 * Extracts the current query and appends another query.
 *
 * @param {String} text the query text passed to the search engine (Apache Lucene syntax)
 */
Application.Search.join = function (text) {
    if (Utils.isEmpty(text)) {
        Application.Search.query("");
    } else {
        let currentText = $("#search").val();
        text += " AND " + currentText;
        Application.Search.query(text);
    }
}

// Bind events
Application.bind("search", Application.Search.query);