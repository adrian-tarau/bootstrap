/*
* The Code Editor  Global Variables
 */
window.CodeEditor = window.CodeEditor || {};

const CODE_EDITOR_MODAL_ID = "editor-modal";

/**
 * Loads a code editor dialog
 *
 * @param {String} path the path
 * @param {Object} [params] the query parameters
 * @param {Object} [options] an optional object, to control how parameters are calculated
 * @see DataSet.get
 */
CodeEditor.loadModal = function (path, params, options) {
    let me = CodeEditor;
    Logger.info("Load dialog, path '" + path + "'");
    me.source = {
        path: path,
        params: params,
        options: options || {},
    };
    DataSet.get(path, params, function (data) {
        me.modal = Application.loadModal(CODE_EDITOR_MODAL_ID, data);
    }, options);
}

/**
 * Saves the content of the editor
 */
CodeEditor.save = function () {
    let me = CodeEditor;
    Logger.info("Save content to path '" + this.source.path + "'");
    let options =  me.source.options;
    options.data = me.editor.getValue()
    DataSet.post(me.source.path, me.source.params, function (data) {
        me.changed = false;
        Application.showInfoAlert("Editor", "The content was saved successfully");
    }, options);
}

/**
 * Copies the editor content to clipboard.
 */
CodeEditor.copy = function () {
    let me = CodeEditor;
    me.editor.copy();
    Application.showInfoAlert("Editor", "Content was copied into clipboard");
}

/**
 * Copies the clipboard content to the editor.
 */
CodeEditor.paste = function () {
    let me = CodeEditor;
    me.editor.paste();
    Application.showInfoAlert("Editor", "Content was copied into editor");
}

/**
 * Closes the editor dialog (if one is opened) and releases the editor.
 */
CodeEditor.close = function () {
    let me = CodeEditor;
    let closeFn = function () {
        if (me.editor) me.editor.destroy();
        if (me.modal) Application.closeModal();
    };
    if (me.changed) {
        Application.question("Editor", "The content of the editor was changed. Are you sure you want to close the editor?", closeFn);
    } else {
        closeFn();
    }

}

/**
 * Initializes the code editor from the loaded content.
 */
CodeEditor.init = function (editor) {
    let me = CodeEditor;
    this.editor = editor;
    editor.session.on('change', function (delta) {
        me.changed = true;
    });
}

/**
 * Returns the current editor.
 */
CodeEditor.getEditor = function () {
    if (!this.editor) {
        let message = "An editor is not available";
        Application.showErrorAlert("Editor", message);
        throw new Error(message);
    }
    return this.editor;
}
