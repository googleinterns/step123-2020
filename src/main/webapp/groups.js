goog.require('goog.dom');
goog.require('goog.dom.classlist');

let newGroupContainer;

function init() {
    newGroupContainer = goog.dom.getElement('new-group-container');
}

function toggleHidden() {
    goog.dom.classlist.toggle(newGroupContainer, 'hidden');
}

function joinGroup(groupId) {
    fetch('/groups',  {
        method: 'PUT',
        body: groupId,
    }).then(response => {if (response.ok) {
        const joinButton = goog.dom.getElement('join-btn-' + groupId);
        goog.dom.setTextContent(joinButton, 'Joined');
        joinButton.setAttribute('disabled', 'true');
    }});
}

function makeCalendarId() {
    const name = document.getElementById('name').value;
    const image = document.getElementById('image').value;
    const description = document.getElementById('description-area').value;

    fetch('/groups?name=' + name + '&image=' + image + '&description=' + description, {
        method: 'POST',
    }).then(response => response.text())
      .then(text => {
        fetch('/calendar?groupId=' + text, {
          method: 'POST',
        });
      }
    );

    location.reload();
    return false;
}
