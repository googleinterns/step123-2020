goog.require('goog.dom');
goog.require('goog.dom.classlist');

let newGroupContainer;

function init() {
    newGroupContainer = goog.dom.getElement("new-group-container");
}

function toggleHidden() {
    goog.dom.classlist.toggle(newGroupContainer, "hidden");
}

function joinGroup(groupId) {
    fetch("/groups",  {
        method: "PUT",
        body: groupId
    });

    const joinButton = goog.dom.getElement("join-btn-" + groupId);
    goog.dom.setTextContent(joinButton, "Joined");
    goog.dom.setProperties(joinButton, {"disabled": "true"});
}
