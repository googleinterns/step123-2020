package com.google.sps.servlets;

import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
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
public class CalendarServlet extends HttpServlet {

  /**
   * Display the Calendar page for each group.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    SoyFileSet sfs = SoyFileSet
        .builder()
        .add(new File("../../src/main/java/templates/calendar.soy"))
        .build();
    SoyTofu tofu = sfs.compileToTofu();

    String out = tofu.newRenderer("templates.calendar.calendarPage").render();
    response.getWriter().println(out);
  }

  /**
   * Create the Calendar page for the group being created
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //TODO: Implement once groups are implemented
    throw new UnsupportedOperationException();
  }
}
