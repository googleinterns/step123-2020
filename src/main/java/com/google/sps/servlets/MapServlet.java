package com.google.sps.servlets;
 
import static com.google.sps.utils.SoyRendererUtils.getOutputString;
import static com.google.sps.utils.StringConstants.*;
 
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.sps.utils.ServletUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 
@WebServlet("/map")
public class MapServlet extends HttpServlet {
 
    /**
     * Adds user's groups as checkboxes to the map drawer
     * Allows users to select which groups they want displayed on the map
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.enforceUserLogin(request, response);
 
        // Reads API Key and HTTP referer from file
        ClassLoader classLoader = MapServlet.class.getClassLoader();
        File apiKeyFile = new File(classLoader.getResource(KEYS_TXT_FILE).getFile());
        Scanner scanner = new Scanner(apiKeyFile);
        final String apiKey = scanner.nextLine();
        scanner.close();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userEmail = request.getUserPrincipal().getName();
        // Stores the IDs for all the groups the user ahs joined
        HashSet<Long> userGroups = new HashSet<Long>();

        Query userQuery = new Query(USER_KIND).addFilter(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, userEmail);
        PreparedQuery userPreparedQuery = datastore.prepare(userQuery);
        Entity user = userPreparedQuery.asSingleEntity();

        if (user != null) {
            userGroups = (HashSet<Long>) user.getProperty(GROUPS_KEY);

        } else {
            // if the user does not exist, create one and add to datastore
            user = new Entity(USER_KIND, userEmail);

            user.setProperty(USER_EMAIL_PROPERTY, userEmail);
            user.setProperty(GROUPS_KEY, userGroups);

            datastore.put(user);
        }

        ImmutableList.Builder<ImmutableMap<String, String>> groupsListBuilder = new ImmutableList.Builder<>();
        for (Long groupId : userGroups) {
            try {
                Entity group = datastore.get(KeyFactory.createKey(GROUP_KIND, groupId));
                ImmutableMap<String, String> groupMap = ImmutableMap.of(
                    GROUP_NAME_PROPERTY, (String) group.getProperty(GROUP_NAME_PROPERTY),
                    GROUP_ID_PROPERTY, group.getKey().getName());
                groupsListBuilder.add(groupMap);
            } catch (EntityNotFoundException invalidGroup) {
                continue;
            }
            
        }
        ImmutableList<ImmutableMap<String, String>> groupsList = groupsListBuilder.build();
 
        final String mapPageHtml = getOutputString(MAP_SOY_FILE, MAP_TEMPLATE_NAMESPACE, 
            ImmutableMap.of(GROUPS_KEY, groupsList, API_KEY_NAME, apiKey));
 
        response.getWriter().println(mapPageHtml);
    }
}
