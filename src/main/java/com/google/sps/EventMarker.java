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
    private String simpleDate;
    private String dateOutput; 
    private String groupName;
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy hh:mm a");
    private static final DateTimeFormatter simpleFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public EventMarker(String name, String description, String location, DateTime date, String groupName) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.groupName = groupName;
        formatDate();
        simpleDate();
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

    /**
     * Formats the Date to a simplified version: 2020-08-13
     */
    private void simpleDate() {
       simpleDate = OffsetDateTime.parse(date.toStringRfc3339()).format(simpleFormat);
    }

    public DateTime getDateTime() {
        return date;
    }

    public String getDateString() {
        return dateOutput; 
    }

 }
