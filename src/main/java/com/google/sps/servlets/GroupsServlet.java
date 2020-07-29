package com.google.sps.servlets;

import static com.google.sps.utils.StringConstants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.sps.utils.ServletUtils;
import com.google.sps.utils.SoyRendererUtils;
import java.io.File;
import java.io.IOException;
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
    // Photo by Hybrid on Unsplash 
    // (https://unsplash.com/@artbyhybrid?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText)
    private final String GROUP_IMAGE = "https://images.unsplash.com/photo-1591622414912-34f2a8f8172e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80";
    private final String GROUP_NAME = "Black Lives Matter";
    
    /**
     * Renders a HTML template with cards representing the various
     * groups that have already been created.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        createSampleGroups(datastore);

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
        //TODO: Unhardcode. Only one hardcoded group will be created and displayed temporarily, but 
        // will change to enable more groups.
        String groupId = createGroup(GROUP_NAME, GROUP_DESCRIPTION, GROUP_IMAGE);

        response.setContentType(CONTENT_TYPE_HTML);
        response.getWriter().println(groupId);
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
     * Handles creating sample groups in datastore if they don't yet exist until user-created groups
     * can be made
     */
    private void createSampleGroups(DatastoreService datastore) {
        try {
            datastore.get(KeyFactory.createKey(GROUP_KIND, "123"));
        } catch(EntityNotFoundException e) {
        // Hard coded groups for now, but hopefully will use user-created groups from datastore
        // Photo by Hybrid on Unsplash 
        // (https://unsplash.com/@artbyhybrid?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText)
        Entity blmEntity = new Entity(GROUP_KIND, "123");
        blmEntity.setProperty(GROUP_NAME_PROPERTY, "Black Lives Matter");
        blmEntity.setProperty(GROUP_IMAGE_PROPERTY, "https://images.unsplash.com/photo-1591622414912-34f2a8f8172" + 
            "e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80");
        blmEntity.setProperty(GROUP_DESCRIPTION_PROPERTY, "Advocating against police brutality and all racially " + 
            "motivated discrimination against Black Americans.");
        datastore.put(blmEntity);
        }

        try {
            datastore.get(KeyFactory.createKey(GROUP_KIND, "456"));
        } catch(EntityNotFoundException e) {
        // Photo by Conscious Design on Unsplash
        // (https://unsplash.com/@conscious_design?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText)
        Entity sierraEntity = new Entity(GROUP_KIND, "456");
        sierraEntity.setProperty(GROUP_NAME_PROPERTY, "Sierra Club");
        sierraEntity.setProperty(GROUP_IMAGE_PROPERTY, "https://images.unsplash.com/photo-1584747167399-06a9ba" + 
            "8302b0?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=2704&q=80");
        sierraEntity.setProperty(GROUP_DESCRIPTION_PROPERTY, "Help protect Earth's natural resources and " + 
            "ensure a healthy environment for future generations.");
        datastore.put(sierraEntity);
        }
    }
}
