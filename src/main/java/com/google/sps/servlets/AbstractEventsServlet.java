package com.google.sps.servlets;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import com.google.sps.utils.ServletUtils;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * Abstract servlet that contains shared variables and functionality between EventsServlet and CalendarServlet.
 */
abstract class AbstractEventsServlet extends HttpServlet {
  protected static final String TIMEZONE = "America/Los_Angeles";

  protected static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  protected static final List<String> SCOPES = ImmutableList.of("https://www.googleapis.com/auth/calendar",
      "https://www.googleapis.com/auth/calendar.events", "https://www.googleapis.com/auth/calendar.events.readonly",
      "https://www.googleapis.com/auth/calendar.readonly", "https://www.googleapis.com/auth/calendar.settings.readonly",
      "https://www.googleapis.com/auth/calendar.events.public.readonly", "https://www.googleapis.com/auth/calendar.app.created");

  protected String getGroupProperty(String groupId, String property) throws EntityNotFoundException {
    Entity groupEntity = getGroupEntity(groupId);

    return (String) groupEntity.getProperty(property);
  }

  protected Entity getGroupEntity(String groupId) throws EntityNotFoundException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      
    Key groupKey = KeyFactory.createKey(ServletUtils.GROUP_CONSTANT, groupId);

    return datastore.get(groupKey);
  }

  protected Calendar getCalendarService() throws IOException {
    final HttpTransport HTTP_TRANSPORT = UrlFetchTransport.getDefaultInstance();

    return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, new AppIdentityCredential(SCOPES))
        .setApplicationName(ServletUtils.APPLICATION_NAME)
        .build();
  }
}
