package com.google.sps.data;

/**
 * Marker of each event address on the map
 * Contains address and event ID
 */
 public class MapEvent {
    private String eventID;
    private String address; 

    public MapEvent(String eventID, String address) {
        this.eventID = eventID;
        this.address = address; 
    } 
 }
 