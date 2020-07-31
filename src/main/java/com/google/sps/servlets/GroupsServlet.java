package com.google.sps.servlets;

import static com.google.sps.utils.ServletUtils.getParameter;
import static com.google.sps.utils.StringConstants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.sps.utils.ServletUtils;
import com.google.sps.utils.SoyRendererUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for displaying and creating groups.
 */
@WebServlet("/groups")
public class GroupsServlet extends HttpServlet {
  private final static Random random = new Random();

  /**
   * Renders a HTML template with cards representing the various
   * groups that have already been created.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    Query groupsQuery = new Query(GROUP_KIND);
    PreparedQuery preparedGroupsQuery = datastore.prepare(groupsQuery); 

    ImmutableList.Builder<ImmutableMap<String, String>> groupsListBuilder = new ImmutableList.Builder<>();
    for (Entity group : preparedGroupsQuery.asIterable()) {
      ImmutableMap<String, String> groupMap = ImmutableMap.of(
          GROUP_NAME_PROPERTY, (String) group.getProperty(GROUP_NAME_PROPERTY),
          GROUP_IMAGE_PROPERTY, (String) group.getProperty(GROUP_IMAGE_PROPERTY),
          GROUP_DESCRIPTION_PROPERTY, (String) group.getProperty(GROUP_DESCRIPTION_PROPERTY));
      groupsListBuilder.add(groupMap);
    }

    ImmutableList<ImmutableMap<String, String>> groupsList = groupsListBuilder.build();

    // Each group has its own map which points to its info and all maps are passed into the template as a list
    // This will make it easier when groups are queried from Datastore
    ImmutableMap<String, ImmutableList<ImmutableMap<String, String>>> groupsData = 
        ImmutableMap.of(GROUPS_KEY, groupsList);

    String groupsPageHtml = SoyRendererUtils.getOutputString(GROUPS_SOY_FILE, GROUPS_TEMPLATE_NAMESPACE, groupsData);

    response.setContentType(CONTENT_TYPE_HTML);
    response.getWriter().println(groupsPageHtml);
  }

  /**
   * Create a group. Parameters are name, description (optional), and image.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String name = getParameter(request, GROUP_NAME_PROPERTY);
    String description = getParameter(request, GROUP_DESCRIPTION_PROPERTY);
    String image = getParameter(request, GROUP_IMAGE_PROPERTY);

    if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(description) || Strings.isNullOrEmpty(image)){
      ServletUtils.printBadRequestError(response, GROUPS_BAD_REQUEST_MESSAGE);
      return;
    }
    
    String groupId = createGroup(name, description, image);

    response.setContentType(CONTENT_TYPE_PLAIN);
    response.getWriter().println(groupId);
  }

  /**
   * Returns the Group ID as a string after creating a group with the user-input parameters.
   */
  private String createGroup(String name, String description, String image){
    long groupId = random.nextLong();
    String groupIdString = String.valueOf(groupId);

    Entity groupEntity = new Entity(GROUP_KIND, groupIdString);

    groupEntity.setProperty(GROUP_NAME_PROPERTY, name);
    groupEntity.setProperty(GROUP_DESCRIPTION_PROPERTY, description);
    groupEntity.setProperty(GROUP_IMAGE_PROPERTY, image);
    groupEntity.setProperty(GROUP_ID_PROPERTY, groupId);

    DatastoreServiceFactory.getDatastoreService().put(groupEntity);

    return groupIdString;
  }
}
