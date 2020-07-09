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
  // TODO: Get the proper scopes. CalendarScopes might be outdated and not have all the proper scopes.
  protected static final List<String> SCOPES = Lists.newArrayList(CalendarScopes.all());

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  protected String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  protected String getGroupProperty(String groupId, String property) throws EntityNotFoundException {
    Entity groupEntity = getGroupEntity(groupId);

    return (String) groupEntity.getProperty(property);
  }

  protected Entity getGroupEntity(String groupId) throws EntityNotFoundException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      
    Key groupKey = KeyFactory.createKey("Group", groupId);

    return datastore.get(groupKey);
  }

  protected Calendar getCalendarService() throws IOException {
    final HttpTransport HTTP_TRANSPORT = UrlFetchTransport.getDefaultInstance();

    return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, new AppIdentityCredential(SCOPES))
        .setApplicationName("The Solidarity Initiative")
        .build();
  }
}
