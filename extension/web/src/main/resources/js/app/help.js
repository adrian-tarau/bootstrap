/*
* The Help Global Variables
 */
window.Help = window.Help || {};

/**
 * Displays a help page.
 *
 * @param {String} title the title of the help article
 * @param {String} path the path
 * @param {String} [anchor] an optional anchor to jump into the document at a specific anchor
 */
Help.open = function (title, path, anchor) {
    Logger.debug("Show help '" + path + "', fra");
    let params = {title: title};
    if (Utils.isNotEmpty(anchor)) params["anchor"] = anchor;
    Application.get("/help/view/" + path, params, function (data) {
        Application.loadModal("help-article", data);
    }, {self: false});
}

/**
 * Moves one page back.
 */
Help.back = function () {
    let frame = this.getIFrame();
    frame.contentWindow.history.back();

}

/**
 * Moves one page forward.
 */
Help.forward = function () {
    let frame = this.getIFrame();
    frame.contentWindow.history.forward();
}

/**
 * Reloads the home page of the current help.
 */
Help.reload = function () {
    let frame = this.getIFrame();
    let url = $(frame).attr("origsrc");
    Logger.debug("Reload help '" + url + "'");
    frame.src = url;
}

/**
 * Returns the IFrame supporting the help.
 *
 * @return {HTMLElement} the IFrame
 */
Help.getIFrame = function () {
    return document.getElementById('help_frame');
}