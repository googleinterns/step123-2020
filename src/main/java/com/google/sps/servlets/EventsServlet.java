package com.google.sps.servlets;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * Servlet for obtaining the Calendar ID of a group and adding or obtaining events
 */
@WebServlet("/events")
public class EventsServlet extends HttpServlet {
  private static final int MAX_EVENTS = 50;

  private static final Gson gson = new Gson();
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  // TODO: Get the proper scopes. CalendarScopes might be outdated and not have all the proper scopes.
  private static final List<String> SCOPES = Lists.newArrayList(CalendarScopes.all());

  /**
   * Obtain a list of events from the specified group if given the Calendar ID.
   * Obtain the Calendar ID of a group given the Group ID. If both provided, return list of events.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String calendarId = getParameter(request, "calendarid", "");
    String groupId = getParameter(request, "groupid", "");

    if (!Strings.isNullOrEmpty(calendarId)) {
      try {
        Calendar service = getCalendarService(); 

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(getEventsList(service, calendarId)));
      } catch (Exception authenticationError) {
        authenticationError.printStackTrace();
        response.getWriter().println("Invalid credentials or calendar ID");
      }
    } else if (!Strings.isNullOrEmpty(groupId)) {
      try {
        calendarId = getCalendarId(groupId);

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
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String groupId = getParameter(request, "groupid", "");
    String eventTitle = getParameter(request, "title", "");
    String eventStart = getParameter(request, "start", "");
    String eventEnd = getParameter(request, "end", "");

    if(!Strings.isNullOrEmpty(groupId) && !Strings.isNullOrEmpty(eventTitle) 
        && !Strings.isNullOrEmpty(eventStart) && !Strings.isNullOrEmpty(eventEnd)) {
      try {
        Event event = addEvent(getCalendarService(), getCalendarId(groupId), 
            eventTitle, getParameter(request, "location", ""),
            getParameter(request, "description", ""), eventStart, eventEnd);

        response.setContentType("text/plain");
        response.getWriter().println(event.getHtmlLink());
      } catch (Exception entityError) {
        entityError.printStackTrace();
        response.getWriter().println("Invalid input");
      }
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

  private Calendar getCalendarService() throws IOException {
    final HttpTransport HTTP_TRANSPORT = UrlFetchTransport.getDefaultInstance();

    return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, new AppIdentityCredential(SCOPES))
        .setApplicationName("The Solidarity Initiative")
        .build();
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

  private String getCalendarId(String groupId) throws EntityNotFoundException {
    Entity groupEntity = getGroupEntity(groupId);

    return (String) groupEntity.getProperty("calendarId");
  }

  private Entity getGroupEntity(String groupId) throws EntityNotFoundException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      
    Key groupKey = KeyFactory.createKey("Group", groupId);

    return datastore.get(groupKey);
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

    EventDateTime eventTime = new EventDateTime().setDateTime(dateTime).setTimeZone("America/Los_Angeles");

    return eventTime;
  }
}
