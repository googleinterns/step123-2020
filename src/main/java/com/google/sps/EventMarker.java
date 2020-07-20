package com.google.sps;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date; 

/**
 * Marker data for each event to be added to the map.
 */
 public class EventMarker {
    private String name;
    private String description;
    private String location; 
    private DateTime date; 
    private String dateOutput; 
    private String groupName;
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy hh:mm a");

    public EventMarker(String name, String description, String location, DateTime date, String groupName) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.groupName = groupName;
        formatDate();
    } 

    public String getName() {
        return name;
    }

    /**
     * Formats the Date ex. Monday, June 08, 2000 12:30PM
     */
    private void formatDate() {      
        dateOutput = OffsetDateTime.parse(date.toStringRfc3339()).format(format); 
    }

    public DateTime getDateTime() {
        return date;
    }

    public String getDateString() {
        return dateOutput; 
    }

 }
