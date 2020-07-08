package com.google.sps.servlets;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Events;
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
import java.security.GeneralSecurityException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * Servlet for getting or creating events
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
        Calendar service = getCalendarService(calendarId); 

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(getEventsList(service, calendarId)));
      } catch (Exception authenticationError) {
        authenticationError.printStackTrace();
        response.getWriter().println("Invalid credentials or calendar ID")
      }
    } else if (!Strings.isNullOrEmpty(groupId)) {
      try {
        Entity group = getGroupEntity(groupId);

        response.setContentType("text/plain");
        response.getWriter().println((String) group.getProperty("calendarId"));
      } catch (Exception entityError) {
        entityError.printStackTrace();
        response.getWriter().println("Invalid group ID")
      }
    }
  }

  /**
   * Create an event and add it to the calendar of the specified group
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //TODO
    throw new UnsupportedOperationException();
  }

  private Calendar getCalendarService(String calendarId) throws IOException {
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

  private Entity getGroupEntity(String groupId) throws EntityNotFoundException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      
    Key groupKey = KeyFactory.createKey("Group", groupId);

    return datastore.get(groupKey);
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
}
