package com.google.sps.servlets;

import static com.google.sps.utils.StringConstants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.sps.utils.ServletUtils;
import java.io.File;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for displaying and creating groups.
 */
@WebServlet("/groups")
public class GroupsServlet extends HttpServlet {
  /**
   * Renders a HTML template with cards representing the various
   * groups that have already been created.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Hard coded data for now but will use data from datastore
    // as groups are added to datastore
    // Photo by Hybrid on Unsplash (https://unsplash.com/@artbyhybrid?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText)
    ImmutableMap<String, String> groupMap = ImmutableMap.of("groupName", "Black Lives Matter",
        "groupImage", "https://images.unsplash.com/photo-1591622414912-34f2a8f8172e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80",
        "groupDescription", "Group description would go here...");
    ImmutableList<ImmutableMap<String, String>> groups = ImmutableList.of(groupMap);
      
    // Each group has its own map which points to its info and all maps are passed into the template as a list
    // This will make it easier when groups are queried from Datastore
    ImmutableMap<String, ImmutableList<ImmutableMap<String, String>>> data = ImmutableMap.of("groups", groups);

    SoyFileSet sfs = SoyFileSet
        .builder()
        .add(new File("../../src/main/java/com/google/sps/templates/groups.soy"))
        .build();
    SoyTofu tofu = sfs.compileToTofu();

    String out = tofu.newRenderer("templates.groups.groupsPage").setData(data).render();
    response.getWriter().println(out);
  }

  /**
   * Create a group. Parameters are name, description (optional), and image.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //TODO: Unhardcode
    String name = "Black Lives Matter";
    String image = "https://images.unsplash.com/photo-1591622414912-34f2a8f8172e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80";

    if(!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(image)) {
      //TODO: Unhardcode
      String groupId = createGroup(name, image, "Group description would go here");

      response.setContentType("text/html");
      response.getWriter().println(groupId);
    }
  }

  /**
   * Returns the Group ID as a string.
   */
  private String createGroup(String name, String image, String description){
    //TODO: Unhardcode
    long groupId = 123L;
    Entity groupEntity = new Entity(GROUP_KIND, String.valueOf(groupId));

    groupEntity.setProperty(GROUP_NAME_PROPERTY, name);
    groupEntity.setProperty(GROUP_IMAGE_PROPERTY, image);
    groupEntity.setProperty(GROUP_DESCRIPTION_PROPERTY, description);
    //TODO: Remove once not hardcoded, since the CalendarServlet deals with setting the calendar ID.
    groupEntity.setProperty(GROUP_CALENDARID_PROPERTY, "fk6u4m5isbl8i6cj1io1pkpli4@group.calendar.google.com");
    //TODO: Unhardcode
    groupEntity.setProperty(GROUP_ID_PROPERTY, groupId);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(groupEntity);

    return String.valueOf(groupId);
  }
}
