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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.sps.utils.ServletUtils;
import com.google.sps.utils.SoyRendererUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
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
        ServletUtils.enforceUserLogin(request, response);
        
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query groupsQuery = new Query(GROUP_KIND);
        PreparedQuery preparedGroupsQuery = datastore.prepare(groupsQuery);

        ImmutableList.Builder<ImmutableMap<String, String>> groupsListBuilder = new ImmutableList.Builder<>();
        for (Entity group : preparedGroupsQuery.asIterable()) {
            ImmutableMap<String, String> groupMap = ImmutableMap.of(
                GROUP_ID_PROPERTY, group.getKey().getName(),
                GROUP_NAME_PROPERTY, (String) group.getProperty(GROUP_NAME_PROPERTY),
                GROUP_IMAGE_PROPERTY, (String) group.getProperty(GROUP_IMAGE_PROPERTY),
                GROUP_DESCRIPTION_PROPERTY, (String) group.getProperty(GROUP_DESCRIPTION_PROPERTY));
            groupsListBuilder.add(groupMap);
        }
        
        Entity userEntity = ServletUtils.getUserEntity(request.getUserPrincipal().getName());
        ImmutableList<ImmutableMap<String, String>> groupsList = groupsListBuilder.build();
        ImmutableList<Long> userGroups = ImmutableList.copyOf(ServletUtils.getGroupIdList(userEntity));
        
        // Each group has its own map which points to its info and all maps are passed into the template as a list
        // This will make it easier when groups are queried from Datastore
        ImmutableMap<String, ImmutableList> groupsData = 
            ImmutableMap.of(GROUPS_KEY, groupsList, USER_GROUPS, userGroups);

        String groupsPageHtml = SoyRendererUtils.getOutputString(GROUPS_SOY_FILE, GROUPS_TEMPLATE_NAMESPACE, groupsData);

        response.setContentType(CONTENT_TYPE_HTML);
        response.getWriter().println(groupsPageHtml);
    }

    /**
     * Create a group. Parameters are name, description, and image.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = getParameter(request, GROUP_NAME_PROPERTY);
        String description = getParameter(request, GROUP_DESCRIPTION_PROPERTY);
        String image = getParameter(request, GROUP_IMAGE_PROPERTY);

        if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(description) || Strings.isNullOrEmpty(image)) {
            ServletUtils.printBadRequestError(response, GROUPS_BAD_REQUEST_MESSAGE);
            return;
        }
        
        String groupId = createGroup(name, description, image);
        
        response.setContentType(CONTENT_TYPE_PLAIN);
        response.getWriter().println(groupId);
    }

    /**
     * Add the group with groupId to a user's list of groups.
     */
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        final String groupId = bufferedReader.readLine();
        
        ServletUtils.enforceUserLogin(request, response);
                
        if (Strings.isNullOrEmpty(groupId)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        try {
            Entity userEntity = ServletUtils.getUserEntity(request.getUserPrincipal().getName());
            addUserToGroup(groupId, userEntity);
            
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception exceptionError) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /*
     * Returns the Group ID as a string after creating a group with the user-input parameters.
     */
    private String createGroup(String name, String description, String image) {
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

    /**
     * Adds user with email to group with groupId. If the user is not in datastore, a new user is created.
     */
    private void addUserToGroup(String groupId, Entity user) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        final long groupIdLong = Long.valueOf(groupId);

        ImmutableList.Builder<Long> groupsListBuilder = new ImmutableList.Builder<>();
        List<Long> datastoreList = (List<Long>) user.getProperty(GROUPS_KEY);
        if (datastoreList != null) {
            // If the user has joined groups, add those as well
            groupsListBuilder.addAll(datastoreList);
        }

        groupsListBuilder.add(groupIdLong);
        user.setProperty(GROUPS_KEY, groupsListBuilder.build());
        datastore.put(user);
    }
}
