package com.google.sps.servlets;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
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

      // All hardcoded tests for the moment 
      // Will call the calendar event function to get the events for the 
      // given groupId once merged

      ArrayList<EventMarker> events = new ArrayList<EventMarker>(); 
        Date date = new Date(2020-1900,8,17,18,30);
        Date date2 = new Date(2020-1900,7,21,1,30);
        Date date3 = new Date(2020-1900,7,31,12,30);
        EventMarker event = new EventMarker("Rally at City Hall", 
            "Join us at San Jose City Hall to rally for the BLM Movement!", 
            "200 E Santa Clara St, San Jose, CA 95113", 
            new DateTime(date), 
            "Black Lives Matter");
        EventMarker event2 = new EventMarker("Peaceful Protest at the Lake", 
            "We're having a peaceful protest at Almaden Lake, come and support!", 
            "6099 Winfield Blvd, San Jose, CA 95120", 
            new DateTime(date2), 
            "Black Lives Matter");
        EventMarker event3 = new EventMarker("Beach BBQ", 
            "Come join us in Santa Cruz to talk and enjoy eachother's company during this time.", 
            "2320 W Cliff Dr, Santa Cruz, CA 95060", 
            new DateTime(date3), 
            "Black Lives Matter");

        events.add(event);
        events.add(event2);
        events.add(event3);

        SortedMarkers sorted = new SortedMarkers(events); 
        return sorted.getSortedMarkers();
  }
}
