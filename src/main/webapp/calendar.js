goog.require('goog.dom');
goog.require('goog.dom.TagName');
goog.require('templates.eventsoy');

function initEvents() {
  addressAutoComplete();
  //TODO: Events POST form
}

function addressAutoComplete() {
  const address = document.getElementById('address');
  const searchBox = new google.maps.places.SearchBox(address);
}
