package com.google.sps.servlets;

import static com.google.sps.utils.StringConstants.*;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServlet;

/**
 * Abstract servlet that contains shared variables and functionality between EventsServlet and CalendarServlet.
 */
abstract class AbstractEventsServlet extends HttpServlet {
  protected static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  protected static final List<String> SCOPES = ImmutableList.of("https://www.googleapis.com/auth/calendar",
      "https://www.googleapis.com/auth/calendar.events", "https://www.googleapis.com/auth/calendar.events.readonly",
      "https://www.googleapis.com/auth/calendar.readonly", "https://www.googleapis.com/auth/calendar.settings.readonly",
      "https://www.googleapis.com/auth/calendar.events.public.readonly", "https://www.googleapis.com/auth/calendar.app.created");

  protected Calendar getCalendarService() throws IOException {
    final HttpTransport HTTP_TRANSPORT = UrlFetchTransport.getDefaultInstance();

    return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, new AppIdentityCredential(SCOPES))
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
