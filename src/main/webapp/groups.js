goog.require('goog.dom');
goog.require('goog.dom.classlist');

let newGroupContainer;

function init() {
    newGroupContainer = goog.dom.getElement("new-group-container");
}

function toggleHidden() {
    goog.dom.classlist.toggle(newGroupContainer, "hidden");
}
