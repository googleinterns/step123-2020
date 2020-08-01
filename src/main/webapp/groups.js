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
    const bodyData = {"groupId": groupId};
    console.log("groupId: " + groupId);
    const response = fetch("/groups" + groupId,  {
        method: "PUT",
        body: JSON.stringify(bodyData)
    });
}
