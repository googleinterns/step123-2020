package com.google.sps.servlets;

import static com.google.sps.utils.ServletUtils.getParameter;
import static com.google.sps.utils.StringConstants.*;

import com.google.api.services.calendar.model.Calendar;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.sps.utils.ServletUtils;
import com.google.sps.utils.SoyRendererUtils;
import java.io.File;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for getting or creating the Calendar page for each group
 */
@WebServlet("/calendar")
public class CalendarServlet extends AbstractEventsServlet {
  /**
   * Display the Calendar page for each group. Query string must contain groupId.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String groupId = getParameter(request, GROUP_ID_PROPERTY);

    if (!Strings.isNullOrEmpty(groupId)){
      String out = SoyRendererUtils.getOutputString(CALENDAR_SOY_FILE, CALENDAR_TEMPLATE_NAMESPACE, null);

      response.setContentType(CONTENT_TYPE_HTML);
      response.getWriter().println(out);
    } else {
      ServletUtils.printBadRequestError(response, CALENDAR_BAD_REQUEST_MESSAGE);
    }
  }

  /**
   * Create a Calendar for the group being created. Query string must contain groupId.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String groupId = getParameter(request, GROUP_ID_PROPERTY);

    if (!Strings.isNullOrEmpty(groupId)){
      try {
        String calendarId = createCalendar(groupId);
        setGroupCalendarId(groupId, calendarId);

        response.setContentType(CONTENT_TYPE_PLAIN);
        response.getWriter().println(calendarId);
      } catch (Exception entityError) {
        response.getWriter().println(ENTITY_ERROR_MESSAGE);
      }
    } else {
      ServletUtils.printBadRequestError(response, CALENDAR_BAD_REQUEST_MESSAGE);
    }
  }

  /**
   * Return the Calendar ID of the created calendar.
   */
  @VisibleForTesting
  public String createCalendar(String groupId) throws IOException, EntityNotFoundException {
    Calendar calendar = new Calendar()
        .setSummary(ServletUtils.getGroupProperty(groupId, GROUP_NAME_PROPERTY)).setTimeZone(TIMEZONE);

    com.google.api.services.calendar.Calendar service = getCalendarService();

    Calendar createdCalendar = service.calendars().insert(calendar).execute();

    return createdCalendar.getId();
  }

  private void setGroupCalendarId(String groupId, String calendarId) throws EntityNotFoundException {
    Entity groupEntity = ServletUtils.getGroupEntity(groupId);
    groupEntity.setProperty(GROUP_CALENDARID_PROPERTY, calendarId);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(groupEntity);
  }
}
