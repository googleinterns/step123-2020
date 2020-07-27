package com.google.sps.servlets;

import static com.google.sps.utils.SoyRendererUtils.getOutputString;
import static com.google.sps.utils.StringConstants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity; 
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/groups")
public class GroupsServlet extends HttpServlet {
    
    /**
     * Renders a HTML template with cards representing the various
     * groups that have already been created.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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
        
        // Photo by Conscious Design on Unsplash
        // (https://unsplash.com/@conscious_design?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText)
        Entity sierraEntity = new Entity(GROUP_KIND, "456");
        sierraEntity.setProperty(GROUP_NAME_PROPERTY, "Sierra Club");
        sierraEntity.setProperty(GROUP_IMAGE_PROPERTY, "https://images.unsplash.com/photo-1584747167399-06a9ba" + 
            "8302b0?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=2704&q=80");
        sierraEntity.setProperty(GROUP_DESCRIPTION_PROPERTY, "Help protect Earth's natural resources and " + 
            "ensure a healthy environment for future generations.");
        datastore.put(sierraEntity);

        Query groupsQuery = new Query(GROUP_KIND);
        PreparedQuery preparedGroupsQuery = datastore.prepare(groupsQuery); 

        ImmutableList.Builder<ImmutableMap<String, String>> builder = new ImmutableList.Builder<>();
        for (Entity group : preparedGroupsQuery.asIterable()) {
            ImmutableMap<String, String> groupMap = ImmutableMap.of(
                GROUP_NAME_PROPERTY, (String) group.getProperty(GROUP_NAME_PROPERTY),
                GROUP_IMAGE_PROPERTY, (String) group.getProperty(GROUP_IMAGE_PROPERTY),
                GROUP_DESCRIPTION_PROPERTY, (String) group.getProperty(GROUP_DESCRIPTION_PROPERTY));
            builder.add(groupMap);
        }
        
        ImmutableList<ImmutableMap<String, String>> groupsList = builder.build();
        
        // Each group has its own map which points to its info and all maps are passed into the template as a list
        // This will make it easier when groups are queried from Datastore
        ImmutableMap<String, ImmutableList<ImmutableMap<String, String>>> groupsData = 
            ImmutableMap.of(GROUPS_KEY, groupsList);

        String groupsPageHtml = getOutputString(GROUPS_SOY_FILE, GROUPS_PAGE_NAMESPACE, groupsData);
        response.getWriter().println(groupsPageHtml);
    }
}
