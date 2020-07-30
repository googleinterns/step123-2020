package com.google.sps.servlets;

import static com.google.sps.utils.ServletUtils.getParameter;
import static com.google.sps.utils.StringConstants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.sps.utils.ServletUtils;
import com.google.sps.utils.SoyRendererUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for displaying and creating groups.
 */
@WebServlet("/groups")
public class GroupsServlet extends HttpServlet {
  //TODO: Unhardcode (remove). Group parameters will be obtained from user input once implemented for more groups.
  private final String GROUP_CALENDARID = "fk6u4m5isbl8i6cj1io1pkpli4@group.calendar.google.com";
  private final String GROUP_DESCRIPTION = "Group for the Black Lives Matter movement.";
  private final long GROUP_ID = 123L;
  private final String GROUP_ID_STRING = "123";
  // Photo by Hybrid on Unsplash (https://unsplash.com/@artbyhybrid?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText)
  private final String GROUP_IMAGE = "https://images.unsplash.com/photo-1591622414912-34f2a8f8172e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80";
  private final String GROUP_NAME = "Black Lives Matter";

  /**
   * Renders a HTML template with cards representing the various
   * groups that have already been created.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Hard coded data for now but will use data from datastore
    // as groups are added to datastore
    try {
      Entity groupEntity = ServletUtils.getGroupEntity(GROUP_ID_STRING);
      Map<String, Object> group = groupEntity.getProperties();
      // Not displaying the group ID, so remove it.
      group.remove(GROUP_ID_PROPERTY);

      // Turn the Map values into Strings rather than Objects for the soy template to read
      ImmutableList<Map<String, String>> groups = ImmutableList.of((Map) group);
      
      // Each group has its own map which points to its info and all maps are passed into the template as a list
      // This will make it easier when groups are queried from Datastore
      ImmutableMap<String, ImmutableList<Map<String, String>>> data = ImmutableMap.of(GROUPS_KEY, groups);

      String htmlString = SoyRendererUtils.getOutputString(GROUPS_SOY_FILE, GROUPS_TEMPLATE_NAMESPACE, data);

      response.setContentType(CONTENT_TYPE_HTML);
      response.getWriter().println(htmlString);
    } catch (Exception entityError) {
       ServletUtils.printBadRequestError(response, ENTITY_ERROR_MESSAGE);
    }
  }

  /**
   * Create a group. Parameters are name, description (optional), and image.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //TODO: Unhardcode. Only one hardcoded group will be created and displayed temporarily, but 
    // will change to enable more groups.
    String groupId = createGroup(GROUP_NAME, GROUP_IMAGE, GROUP_DESCRIPTION);

    response.setContentType(CONTENT_TYPE_PLAIN);
    response.getWriter().println(groupId);
  }

  /**
   * Add the group with groupId to the user's list of groups.
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ServletUtils.enforceUserLogin(request, response);
    String groupId = getParameter(request, GROUP_ID_PROPERTY);
    
    if (Strings.isNullOrEmpty(groupId)) {
      ServletUtils.printBadRequestError(response, INVALID_GROUPID_BAD_REQUEST_MESSAGE);
      return;
    }

    try {
      addUserToGroup(request.getUserPrincipal().getName(), groupId);

      response.setContentType(CONTENT_TYPE_PLAIN);
      response.getWriter().println(groupId);
    } catch (Exception exceptionError) {
      ServletUtils.printBadRequestError(response, ENTITY_ERROR_MESSAGE);
      return;
    }

  }

  /**
   * Returns the Group ID as a string after creating a group with the default parameters.
   * TODO: Change to take in parameters once user input groups are implemented
   */
  private String createGroup(String name, String description, String image){
    Entity groupEntity = new Entity(GROUP_KIND, GROUP_ID_STRING);

    groupEntity.setProperty(GROUP_NAME_PROPERTY, name);
    groupEntity.setProperty(GROUP_DESCRIPTION_PROPERTY, description);
    //TODO: Remove once not hardcoded, since the CalendarServlet deals with setting the calendar ID.
    groupEntity.setProperty(GROUP_CALENDARID_PROPERTY, GROUP_CALENDARID);
    groupEntity.setProperty(GROUP_ID_PROPERTY, GROUP_ID);
    groupEntity.setProperty(GROUP_IMAGE_PROPERTY, image);

    DatastoreServiceFactory.getDatastoreService().put(groupEntity);

    return GROUP_ID_STRING;
  }

  /**
   * Adds user with email to group with groupId. If the user is not in datastore, a new user is created.
   */
  private void addUserToGroup(String groupId, String email) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    try {
      Entity user = datastore.get(KeyFactory.createKey(USER_KIND, email));

      HashSet<Long> groups = (HashSet<Long>) user.getProperty(GROUPS_KEY);
      groups.add(Long.valueOf(groupId));
      user.setProperty(GROUPS_KEY, groups);

      datastore.put(user);
    } catch (EntityNotFoundException newUser) {
      Entity user = new Entity(USER_KIND, email);

      user.setProperty(USER_EMAIL_PROPERTY, email);
      user.setProperty(GROUPS_KEY, new HashSet<Long>());

      datastore.put(user);
    }
  }
}
