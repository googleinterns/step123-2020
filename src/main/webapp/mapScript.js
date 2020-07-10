goog.require('goog.dom');
goog.require('goog.dom.TagName');

// Array that will hold the groups and there markers in a 2D Array
// The array will allow the markers to be easilly all disabled as 
// Groups were unchecked or checked 
let groupArray = []; 

/**
 * Initialize the map to show Mountain View and events in that area
 */
function initMap() {
  
    // Coordinates for the default map center of Mountain View, CA
    const mapViewDefault = {lat: 37.3868, lng: -122.085}; 

    const map = new google.maps.Map(goog.dom.getElement("map"), {
    // Zoom set to 8 as default until the radius is set up with events
        zoom: 8,
        center: mapViewDefault,
    });

    const geocoder = new google.maps.Geocoder();
    // TODO: repace the hardcoded event ID with 
    getMarkerInfo(123, geocoder, map);
    document.getElementById('submit').addEventListener('click', () => {
        geocodeAddress(geocoder, map);
    });
}


/**
 * Finds the location on the map for the given address
 */
function geocodeAddress(geocoder, resultsMap) {
    const address = document.getElementById('address').value;
    geocoder.geocode({'address': address}, (results, status) => {
        if (status === 'OK') {
            resultsMap.setCenter(results[0].geometry.location);
        } else {
            alert('Geocode was not successful for the following reason: ' + status);
        }
    });
}

/**
 * Determines which group was checked or unchecked
 * Then sends that group to either be removed from the map if unchecked
 * Or added to the map if checked
 */
function addGroupChecked(checkbox, groupId) {
    // Get the group name from the checkbox that was checked 
    if (checkbox.checked) {
        window.alert('Checked group:' + groupId);
        addGroupMarkers(groupId);
    } else {
        window.alert('Unchecked group:' + groupId);
        removeGroupMarkers(groupId);
    }
}

/**
 * Adds a group and its events to the map based on the group passed
 */
function addGroupMarkers(groupId) {
    // Get event's with GroupID 
    // For each event in the group 
    // Call getMarkerInfo on eventId
}

/**
 * Removes the markers of the given group from the map
 */
function removeGroupMarkers(groupId) { 
    // For each event in the group,
    // hide each of the markers on map
}

/**
 * Fetch all the events to add to the map 
 */
function getMarkerInfo(eventId, geocoder, map) {
    // Fetch the json with the eventMarker objects 
    // For each object call addMarker()
    const address = 'San Diego, CA'; 
    const description = 'This is the hardcoded test description';
    const name = 'Test event';
    
    geocoder.geocode({'address': address}, (results) => {
        // Geocoded the address to longitude and lattitude for
        // Marker placement
        addMarker(results[0].geometry.location, description, name, map);
    });
}

/**
 * Add markers onto the map 
 * Using the eventID get the name and description for
 * the infoWindow 
 */
function addMarker(address, descriptionText, nameText, map) {
    const description = goog.dom.createDom(goog.dom.TagName.P);
    goog.dom.setTextContent(description, descriptionText); 

    const name = goog.dom.createDom(goog.dom.TagName.H2);
    goog.dom.setTextContent(name, nameText) 

    const infoContent =  goog.dom.createDom(goog.dom.TagName.DIV, null, name, description);
    const infoWindow = new google.maps.InfoWindow({
        content: infoContent,
    });
 
    let marker = new google.maps.Marker({
        position: address, 
        map: map, 
        title: nameText,
    });
      
    marker.addListener('click', () => infoWindow.open(map,marker));
}
