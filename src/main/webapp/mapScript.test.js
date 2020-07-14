const addToGroupMarkers = require('./mapScript');

test('if addToGroupMarkers function exists', () => {
    expect(typeof addToGroupMarkers).toEqual('function');
});

test('Add map test', () => {
    const map = new google.maps.Map(goog.dom.getElement('map'), {
        zoom: 10,
        center: {lat: 13, lng: -122},
    });
    const marker1 = new google.maps.Marker({
        position: {lat: 15, lng: -122}, 
        map: map, 
        title: 'One Marker',
    });
    const marker2 = new google.maps.Marker({
        position: {lat: 11, lng: -122}, 
        map: map, 
        title: 'Two Marker',
    });
    const groupId = '123'; 
    let expected = new Map(); 
    expected.set(groupId, new Array(marker1, marker2)); 

    addToGroupMarkers(groupId, marker1);
    addToGroupMarkers(groupId, marker2);

    expect(groupMarkers).toEqual(expected);
});