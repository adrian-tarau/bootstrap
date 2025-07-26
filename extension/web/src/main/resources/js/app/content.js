/*
* The Content Variables
 */
window.Content = window.Content || {};

/**
 * Downloads the content.
 * @return {string} id the identifier of the content
 */
Content.download = function (id) {
    let uri = Application.getUri("/content/get/" + id, {"download": "true"},{self:false});
    let iframe =$('<iframe/>', {
        src: uri,
        class: "d-none"
    }).appendTo('body');
}