goog.require('goog.dom');
goog.require('goog.dom.TagName');
goog.require('templates.infoWindow');

// Array that will hold the groups and there markers in a 2D Array
// The array will allow the markers to be easilly all disabled as 
// Groups were unchecked or checked 
let groupMarkers = new Map(); 

// Default map placement centered at Mountain View, CA
const DEFAULT_LAT = 37.3868;
const DEFAULT_LNG = -122.085;
const DEFAULT_ZOOM = 10;
 
let map; 

/**
 * Initialize the map to show Mountain View and events in that area
 */
function initMap() {
    const mapViewDefault = {lat: DEFAULT_LAT, lng: DEFAULT_LNG}; 

    map = new google.maps.Map(goog.dom.getElement('map'), {
        // Zoom level will change automatically with user's search
        zoom: DEFAULT_ZOOM,
        center: mapViewDefault,
    });

    const geocoder = new google.maps.Geocoder();
    // TODO: repace the hardcoded event ID with actually value
    getMarkerInfo('123', geocoder);

    autoCompleteAndZoom(); 

    document.getElementById('submit').addEventListener('click', () => {
        geocodeAddress(geocoder);
    });

}

/**
 * Adds the auto Complete for the address search bar 
 * Allows user to hit enter instead of button
 * Automatically zooms according to the location inputed
 */
function autoCompleteAndZoom() {
    const address = document.getElementById('address');
    let searchBox = new google.maps.places.SearchBox(address);

    map.addListener("bounds_changed", function() {
        searchBox.setBounds(map.getBounds());
    });

    searchBox.addListener("places_changed", function() {
        const places = searchBox.getPlaces();

        // Checks if there are not autocomplete options
        if (places.length == 0) {
            return;
        }

        let bounds = new google.maps.LatLngBounds();
        places.forEach(function(place) {
            if (!place.geometry) {
                console.log("Returned place contains no geometry");
                return;
            }

            // Get the bounds for the location
            if (place.geometry.viewport) {
                bounds.union(place.geometry.viewport);
            } else {
                bounds.extend(place.geometry.location);
            }
        });
        map.fitBounds(bounds);
    }); 
}

/**
 * Finds the location on the map for the given address
 */
function geocodeAddress(geocoder) {
    const address = document.getElementById('address').value;

    geocoder.geocode({'address': address}, (results, status) => {
        if (status === 'OK') {
            map.setCenter(results[0].geometry.location);
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
function toggleGroupMarkers(checkbox, groupId) {
    // Get the group name from the checkbox that was checked 
    if (checkbox.checked) {
        addGroupMarkers(groupId);
    } else {
        removeGroupMarkers(groupId);
    }
}

/**
 * Adds a group and its events to the map based on the group passed
 */
function addGroupMarkers(groupId) {
    if(groupMarkers.has(groupId)) {
        setMapMarkers(map, groupId);
    }
}

/**
 * Removes the markers of the given group from the map
 */
function removeGroupMarkers(groupId) { 
    if(groupMarkers.has(groupId)) {
        setMapMarkers(null, groupId);
    }
}

/**
 * Fetch all the events to add to the map 
 */
function getMarkerInfo(groupId, geocoder) {
    // Fetch the json with the eventMarker objects 
    // For each object call addMarker()
    const address = 'San Diego, CA'; 
    const description = 'This is the hardcoded test description';
    const name = 'Test event';
    
    geocoder.geocode({'address': address}, (results) => {
        // Geocoded the address to longitude and lattitude for
        // Marker placement
        addMarkerToMap(results[0].geometry.location, description, name, groupId);
    });
}

/**
 * Add markers onto the map 
 * Using the eventID get the name and description for
 * the infoWindow 
 */
function addMarkerToMap(address, descriptionText, nameText, groupId) {
    const infoContent = templates.infoWindow.getMarkerInfo({
        'name': nameText, 
        'description': descriptionText,
    });

    const infoWindow = new google.maps.InfoWindow({
        content: goog.dom.constHtmlToNode(goog.string.Const.from(infoContent)),
    });
 
    const marker = new google.maps.Marker({
        position: address, 
        map: map, 
        title: nameText,
    });
      
    marker.addListener('click', () => infoWindow.open(map,marker));
    addToGroupMarkers(groupId, marker);
}

/**
 * Add marker to the list with the groupId as the key
 * Create add the key/value pair if not already there
 */
function addToGroupMarkers(groupId, marker) {
    if (!(groupMarkers.has(groupId))) {
        groupMarkers.set(groupId, new Array(marker));
    } else {
        groupMarkers.get(groupId).add(marker); 
    }
}

// Sets the map on all markers in the array.
function setMapMarkers(map, groupId) {
    const markers = groupMarkers.get(groupId);
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(map);
    }
}

module.exports = addToGroupMarkers;