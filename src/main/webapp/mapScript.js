
function initMap() {

  const mapViewDefault = {lat: 37.3868, lng: -122.085}; 

  const map = new google.maps.Map(document.getElementById('map'), {
    zoom: 8,
    center: mapViewDefault
  });
  
  const geocoder = new google.maps.Geocoder();

  document.getElementById('submit').addEventListener('click', function() {
    geocodeAddress(geocoder, map);
  });

}


/**
 * Finds the location on the map for the given address
 */
function geocodeAddress(geocoder, resultsMap) {
  let address = document.getElementById('address').value;
  geocoder.geocode({'address': address}, function(results, status) {
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
