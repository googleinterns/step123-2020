goog.require('goog.dom');
goog.require('goog.style');

let newGroupContainer;

function init() {
    newGroupContainer = goog.dom.getElement("new-group-container");
}

function showForm() {
    goog.style.setStyle(newGroupContainer, "display", "flex");
}

function hideForm() {
    goog.style.setStyle(newGroupContainer, "display", "none");
}
