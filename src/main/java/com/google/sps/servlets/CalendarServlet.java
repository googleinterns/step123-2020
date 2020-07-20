package com.google.sps.servlets;

import com.google.api.services.calendar.model.Calendar;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.base.Strings;
import com.google.sps.utils.ServletUtils;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.jbcsrc.api.SoySauce;
import com.google.template.soy.tofu.SoyTofu;
import java.io.File;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.sps.utils.ServletUtils.getParameter;

/**
 * Servlet for getting or creating the Calendar page for each group
 */
@WebServlet("/calendar")
public class CalendarServlet extends AbstractEventsServlet {
  private final ClassLoader classLoader = CalendarServlet.class.getClassLoader();

  /**
   * Display the Calendar page for each group.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    SoyFileSet sfs = SoyFileSet
        .builder()
        .add(new File(classLoader.getResource("calendar.soy").getFile()))
        .build();
    SoySauce sauce = sfs.compileTemplates();

    String out = sauce.renderTemplate("templates.calendar.calendarPage").render().get();
    response.getWriter().println(out);
  }

  /**
   * Create a Calendar for the group being created
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String groupId = getParameter(request, "groupid");

    if(!Strings.isNullOrEmpty(groupId)){
      try {
        String calendarId = createCalendar(groupId);

        response.setContentType("text/plain");
        response.getWriter().println(calendarId);
      } catch (Exception entityError) {
        entityError.printStackTrace();
        response.getWriter().println("Invalid input");
      }
    }
  }

  /**
   * Return the Calendar ID of the created calendar.
   */
  private String createCalendar(String groupId) throws IOException, EntityNotFoundException {

    Calendar calendar = new Calendar()
        .setSummary(getGroupProperty(groupId, "name")).setTimeZone(TIMEZONE);

    com.google.api.services.calendar.Calendar service = getCalendarService();

    Calendar createdCalendar = service.calendars().insert(calendar).execute();

    return createdCalendar.getId();
  }
}
