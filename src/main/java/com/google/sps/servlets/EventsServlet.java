package com.google.sps.servlets;

import static com.google.sps.utils.ServletUtils.getParameter;
import static com.google.sps.utils.StringConstants.*;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.common.annotations.VisibleForTesting;
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
  private final String CALENDAR_REDIRECT = "/calendar?groupId=";
  private final String SORT_EVENTS_BY = "startTime";
  private final int MAX_EVENTS = 50;
  private final long MS_IN_ONE_HOUR = 3600000L;
  private final long MS_IN_ONE_MINUTE = 60000L;

  /**
   * Obtain a list of events from the specified group if given the Calendar ID.
   * This value should be provided in the query string.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String calendarId = getParameter(request, GROUP_CALENDARID_PROPERTY);

    if (Strings.isNullOrEmpty(calendarId)) {
      ServletUtils.printBadRequestError(response, EVENTS_GET_BAD_REQUEST_MESSAGE);
      return;
    }

    try {
      Events events = getEventsList(calendarId); 

      response.setContentType(CONTENT_TYPE_JSON);
      response.getWriter().println(events.toString());
    } catch (Exception authenticationError) {
      response.getWriter().println(EVENTS_GET_INVALID_CALENDARID_MESSAGE);
    }
  }

  /**
   * Create an event and add it to the calendar of the specified group. Query string should contain groupId
   * POST body should contain: title, description (opt), location (opt), start time, and duration (hours and mins).
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String groupId = getParameter(request, GROUP_ID_PROPERTY);
    String eventTitle = getParameter(request, EVENT_TITLE_PROPERTY);
    String eventStart = getParameter(request, EVENT_START_PROPERTY);
    String eventEnd = getEventEndTime(eventStart, getParameter(request, EVENT_HOURS_PROPERTY), getParameter(request, EVENT_MINUTES_PROPERTY));

    if (Strings.isNullOrEmpty(groupId) || Strings.isNullOrEmpty(eventTitle) 
        || Strings.isNullOrEmpty(eventEnd)) {
      ServletUtils.printBadRequestError(response, EVENTS_POST_BAD_REQUEST_MESSAGE);
      return;
    }

    try {
      Event event = addEvent(ServletUtils.getGroupProperty(groupId, GROUP_CALENDARID_PROPERTY), 
          eventTitle, getParameter(request, EVENT_LOCATION_PROPERTY),
          getParameter(request, EVENT_DESCRIPTION_PROPERTY), eventStart + TIMEZONE_OFFSET, eventEnd);

      response.setContentType(CONTENT_TYPE_JSON);
      response.getWriter().println(event.toString());
    } catch (Exception entityError) {
      response.getWriter().println(ENTITY_ERROR_MESSAGE);
    }
    response.sendRedirect(CALENDAR_REDIRECT + groupId);
  }

  /**
   * Called from another servlet to get the events rather than through a GET request.
   * Return null if invalid groupId.
   */
  public Events getEventsList(String groupId) throws IOException {
    String calendarId = "";

    try {
      calendarId = ServletUtils.getGroupProperty(groupId, GROUP_CALENDARID_PROPERTY);
    } catch (Exception entityError) {
      return null;
    }

    return getEventsList(getCalendarService(), calendarId);
  }

  private Events getEventsList(Calendar service, String calendarId) throws IOException {
    DateTime now = new DateTime(System.currentTimeMillis());

    return service.events().list(calendarId)
        .setTimeMin(now)
        .setMaxResults(MAX_EVENTS)
        .setOrderBy(SORT_EVENTS_BY)
        .setSingleEvents(true)
        .execute();
  }
  
  @VisibleForTesting
  public Event addEvent(String calendarId, String eventTitle, String eventLocation,
      String eventDescription, String eventStart, String eventEnd) throws IOException {
    Event event = createEvent(eventTitle, eventLocation, eventDescription, eventStart, eventEnd);
    
    Calendar service = getCalendarService();
    service.events().insert(calendarId, event).execute();

    return event;
  }

  private Event createEvent(String eventTitle, String eventLocation, String eventDescription,
     String eventStart, String eventEnd) {
    EventDateTime startTime = createEventDateTime(eventStart);
    EventDateTime endTime = createEventDateTime(eventEnd);

    return new Event().setSummary(eventTitle).setLocation(eventLocation).setDescription(eventDescription)
        .setStart(startTime).setEnd(endTime);
  }

  private EventDateTime createEventDateTime(String time) {
    return new EventDateTime().setDateTime(new DateTime(time)).setTimeZone(TIMEZONE);
  }

  @VisibleForTesting
  public String getEventEndTime(String startTime, String hours, String minutes) {
    if(Strings.isNullOrEmpty(startTime) || Strings.isNullOrEmpty(hours) || Strings.isNullOrEmpty(minutes)) {
      return null;
    }
    
    //Add the start time in milliseconds to the amount of hours and minutes.
    long endTimeMs = new DateTime(startTime + TIMEZONE_OFFSET).getValue() + 
        (Long.valueOf(hours) * MS_IN_ONE_HOUR) + (Long.valueOf(minutes) * MS_IN_ONE_MINUTE);

    return new DateTime(endTimeMs, TIMEZONE_OFFSET_INT).toStringRfc3339();
  }
}
