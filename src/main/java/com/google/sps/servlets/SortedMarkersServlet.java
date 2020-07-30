package com.google.sps.servlets;
 
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.util.DateTime;
import java.util.Date;
import java.util.ArrayList;
import com.google.common.base.Strings;
import com.google.sps.EventMarker;
import com.google.sps.SortedMarkers;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
@WebServlet("/sortedMarkers")
public class SortedMarkersServlet extends HttpServlet {
 
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    String groupId = getParameter(request, "groupid", "");
    
    if(!Strings.isNullOrEmpty(groupId)){ 
        SortedMarkers sorted = new SortedMarkers(getEvents(groupId));
        Collection<EventMarker> output = sorted.getSortedMarkers(); 
        
        // Convert the sortedMarkers to JSON
        String jsonResponse = gson.toJson(output);
 
        // Send the JSON back as the response
        response.setContentType("application/json");
        response.getWriter().println(jsonResponse);
    }
  }
 
  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
 
  /**
   * Get the calendar events for the specific group
   * Convert the events to EventMarker objects
   */
  private Collection<EventMarker> getEvents(String groupId) {
    try {
        ArrayList<EventMarker> mapEvents = new ArrayList<EventMarker>();
        EventsServlet eventsServlet = new EventsServlet();
        
        // CalendarID and group Name are both hardcoded for now
        // The events are from the calendar though
        Events calEvents = eventsServlet.getEventsList("fk6u4m5isbl8i6cj1io1pkpli4@group.calendar.google.com");
        for (Event calEvent : calEvents.getItems()) {
            EventMarker mapEvent = new EventMarker(
                calEvent.getSummary(),
                calEvent.getDescription(),
                calEvent.getLocation(),
                calEvent.getStart().getDateTime(),
                "Black Lives Matter");
            mapEvents.add(mapEvent);
        }
        SortedMarkers sortedEvents = new SortedMarkers(mapEvents); 
        return sortedEvents.getSortedMarkers();
        
    } catch (IOException e) {
        e.printStackTrace(); 
        return null; 
    }
  }
}
