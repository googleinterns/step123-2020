package com.google.sps.servlets;

import static com.google.sps.utils.ServletUtils.getParameter;
import static com.google.sps.utils.StringConstants.*;

import com.google.api.services.calendar.model.Acl;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.AclRule.Scope;
import com.google.api.services.calendar.model.Calendar;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.sps.utils.ServletUtils;
import com.google.sps.utils.SoyRendererUtils;
import com.google.sps.servlets.GroupsServlet;
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
  private final String SCOPE_TYPE = "default";
  private final String USER_CALENDAR_PERMISSIONS = "reader";

  /**
   * Display the Calendar page for each group.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ImmutableList<ImmutableMap<String, String>> groupsList = 
        ServletUtils.getGroupsList(ServletUtils.getUserEntity(request.getUserPrincipal().getName()));

    String groupId = getParameter(request, GROUP_ID_PROPERTY);

    if (groupsList.isEmpty()) {
      groupId = "";
    } else if (Strings.isNullOrEmpty(groupId)) {
      groupId = groupsList.get(0).get(GROUP_ID_PROPERTY);
    }

    String calendarId = "";
    if (!Strings.isNullOrEmpty(groupId)) {
      try {
        calendarId = ServletUtils.getGroupProperty(groupId, GROUP_CALENDARID_PROPERTY);
      } catch (Exception entityError) {
        ServletUtils.printBadRequestError(response, ENTITY_ERROR_MESSAGE);
        return;
      }
    }

    if(Strings.isNullOrEmpty(calendarId)) {
      ServletUtils.printBadRequestError(response, ENTITY_ERROR_MESSAGE);
      return;
    }
    
    String htmlString = SoyRendererUtils.getOutputString(CALENDAR_SOY_FILE, CALENDAR_TEMPLATE_NAMESPACE,
        ImmutableMap.of(GROUP_CALENDARID_PROPERTY, calendarId, CURR_GROUP_KEY, groupId,
            GROUPS_KEY, groupsList, TIMEZONE_PARAM, TIMEZONE));

    response.setContentType(CONTENT_TYPE_HTML);
    response.getWriter().println(htmlString);
  }

  /**
   * Create the Calendar page for the group being created
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String groupId = getParameter(request, GROUP_ID_PROPERTY);

    if (Strings.isNullOrEmpty(groupId)){
      ServletUtils.printBadRequestError(response, INVALID_GROUPID_BAD_REQUEST_MESSAGE);
      return;
    }

    try {
      String calendarId = createCalendar(groupId);
      setGroupCalendarId(groupId, calendarId);

      response.setContentType(CONTENT_TYPE_PLAIN);
      response.getWriter().println(calendarId);
    } catch (Exception entityError) {
      response.getWriter().println(ENTITY_ERROR_MESSAGE);
    }
  }

  /**
   * Return the Calendar ID after creating a calendar for the given groupId.
   */
  @VisibleForTesting
  public String createCalendar(String groupId) throws IOException, EntityNotFoundException {
    Calendar calendar = new Calendar()
        .setSummary(ServletUtils.getGroupProperty(groupId, GROUP_NAME_PROPERTY)).setTimeZone(TIMEZONE);

    com.google.api.services.calendar.Calendar service = getCalendarService();

    String createdCalendarId = service.calendars().insert(calendar).execute().getId();
    
    // Enable reader permission for user
    AclRule rule = new AclRule().setScope(new Scope().setType(SCOPE_TYPE)).setRole(USER_CALENDAR_PERMISSIONS);
    service.acl().insert(createdCalendarId, rule).execute();

    return createdCalendarId;
  }

  private void setGroupCalendarId(String groupId, String calendarId) throws EntityNotFoundException {
    Entity groupEntity = ServletUtils.getGroupEntity(groupId);
    groupEntity.setProperty(GROUP_CALENDARID_PROPERTY, calendarId);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(groupEntity);
  }
}
