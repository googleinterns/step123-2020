package com.google.sps.servlets;

import static com.google.sps.utils.ServletUtils.getParameter;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.common.base.Strings;
import com.google.sps.utils.ServletUtils;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for obtaining the Calendar ID of a group and adding or obtaining events
 */
@WebServlet("/events")
public class EventsServlet extends AbstractEventsServlet {
  private static final int MAX_EVENTS = 50;
  private static final String CALENDAR_ID_PARAM = "calendarId";

  /**
   * Obtain a list of events from the specified group if given the Calendar ID.
   * Obtain the Calendar ID of a group given the Group ID. If both provided, return list of events.
   * These values should be in the query string.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String calendarId = getParameter(request, CALENDAR_ID_PARAM);
    String groupId = getParameter(request, "groupid");

    if (!Strings.isNullOrEmpty(calendarId)) {
      try {
        Events events = getEventsList(getCalendarService(), calendarId); 

        response.setContentType("application/json;");
        response.getWriter().println(ServletUtils.gson.toJson(events));
      } catch (Exception authenticationError) {
        authenticationError.printStackTrace();
        response.getWriter().println("Invalid credentials or calendar ID");
      }
    } else if (!Strings.isNullOrEmpty(groupId)) {
      try {
        calendarId = getGroupProperty(groupId, CALENDAR_ID_PARAM);

        response.setContentType("text/plain");
        response.getWriter().println(calendarId);
      } catch (Exception entityError) {
        entityError.printStackTrace();
        response.getWriter().println("Invalid group ID");
      }
    }
  }

  /**
   * Create an event and add it to the calendar of the specified group
   * POST body should contain: title, location (opt), description (opt), start time, and end time.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String groupId = getParameter(request, "groupid");
    String eventTitle = getParameter(request, "title");
    String eventStart = getParameter(request, "start");
    String eventEnd = getParameter(request, "end");

    if(!Strings.isNullOrEmpty(groupId) && !Strings.isNullOrEmpty(eventTitle) 
        && !Strings.isNullOrEmpty(eventStart) && !Strings.isNullOrEmpty(eventEnd)) {
      try {
        Event event = addEvent(getCalendarService(), getGroupProperty(groupId, CALENDAR_ID_PARAM), 
            eventTitle, getParameter(request, "location"),
            getParameter(request, "description"), eventStart, eventEnd);

        response.setContentType("text/plain");
        response.getWriter().println(event.getHtmlLink());
      } catch (Exception entityError) {
        entityError.printStackTrace();
        response.getWriter().println("Invalid input");
      }
    }
  }

  private Events getEventsList(Calendar service, String calendarId) throws IOException {
    DateTime now = new DateTime(System.currentTimeMillis());

    return service.events().list(calendarId)
        .setTimeMin(now)
        .setMaxResults(MAX_EVENTS)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute();
  }

  /**
   * Called from another servlet to get the events rather than through a GET request
   */
  public Events getEventsList(String calendarId) throws IOException {
    return getEventsList(getCalendarService(), calendarId);
  }

  private Event addEvent(Calendar service, String calendarId, String eventTitle, String eventLocation,
      String eventDescription, String eventStart, String eventEnd) throws IOException {
    EventDateTime startTime = createEventDateTime(eventStart);
    EventDateTime endTime = createEventDateTime(eventEnd);

    Event event = new Event().setSummary(eventTitle).setLocation(eventLocation)
        .setDescription(eventDescription).setStart(startTime).setEnd(endTime);

    service.events().insert(calendarId, event).execute();

    return event;
  }

  private EventDateTime createEventDateTime(String time) {
    DateTime dateTime = new DateTime(time);

    return new EventDateTime().setDateTime(dateTime).setTimeZone(TIMEZONE);
  }
}
