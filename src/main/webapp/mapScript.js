

/**
 * Initialize the map to show Mountain View and events in that area
 */
function initMap() {
  
  // Coordinates for the default map center of Mountain View, CA
  const mapViewDefault = {lat: 37.3868, lng: -122.085}; 

  const map = new google.maps.Map(document.getElementById('map'), {
    // Zoom set to 8 as default until the radius is set up
    zoom: 8,
    center: mapViewDefault,
  });

  document.getElementById('submit').addEventListener('click', () => {
    geocodeAddress(new google.maps.Geocoder(), map);
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
 * Add markers onto the map 
 * Using the eventID get the name and description for
 * the infoWindow 
 */
function addMarker(eventID, address, geocoder) {
  // TODO call event getter methods with eventID 
  // Will get the name and description to add to the infoWindow
}
