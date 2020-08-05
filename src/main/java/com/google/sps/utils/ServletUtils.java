package com.google.sps.utils;

import static com.google.sps.utils.StringConstants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class contains methods and constants shared by the Servlets.
 */
public final class ServletUtils {
    public static final String DEFAULT_PARAM = "";
  
    /**
     * @return the request parameter, or DEFAULT_PARAM if the parameter
     *         was not specified by the client
     */
    public static String getParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        
        return Strings.isNullOrEmpty(value) ? DEFAULT_PARAM : value;
    }

    public static Entity getGroupEntity(String groupId) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        Key groupKey = KeyFactory.createKey(GROUP_KIND, groupId);

        return datastore.get(groupKey);
    }

    /**
     * If the property is invalid, will return null. 
     */
    public static String getGroupProperty(String groupId, String property) throws EntityNotFoundException {
        Entity groupEntity = getGroupEntity(groupId);

        return (String) groupEntity.getProperty(property);
    }
  
    public static void printBadRequestError(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType(CONTENT_TYPE_PLAIN);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().println(errorMessage);
    }

    public static void enforceUserLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(request.getUserPrincipal() == null) {
            // If user is not logged in, redirect to index page.
            response.sendRedirect("/");
            return;
        }
    
    }

    public static Entity getUserEntity(String userEmail) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query userQuery = new Query(USER_KIND).addFilter(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, 
            KeyFactory.createKey(USER_KIND, userEmail));
        PreparedQuery userPreparedQuery = datastore.prepare(userQuery);
        Entity user = userPreparedQuery.asSingleEntity();

        if (user == null) {
            // if the user does not exist, create one and add to datastore
            user = new Entity(USER_KIND, userEmail);
            user.setProperty(USER_EMAIL_PROPERTY, userEmail);
            user.setProperty(GROUPS_KEY, ImmutableList.of());

            datastore.put(user);
        }
        return user;
    }

    public static ImmutableList<Long> getGroupIdList(Entity user) {
        ImmutableList<Long> userGroups = ImmutableList.of();

        List<Long> datastoreList = (List<Long>) user.getProperty(GROUPS_KEY);
        if (datastoreList != null) {
            // If the user has joined groups, get those instead
            userGroups = ImmutableList.copyOf(datastoreList);
        }

        return userGroups;
    }

    public static ImmutableList<ImmutableMap<String, String>> getGroupsList(Entity user) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Stores the IDs for all the groups the user has joined
        List<Long> userGroups = getGroupIdList(user);

        ImmutableList.Builder<ImmutableMap<String, String>> groupsListBuilder = new ImmutableList.Builder<>();
        for (Long groupId : userGroups) {
            try {
                Entity group = datastore.get(KeyFactory.createKey(GROUP_KIND, String.valueOf(groupId)));
                ImmutableMap<String, String> groupMap = ImmutableMap.of(
                    GROUP_NAME_PROPERTY, (String) group.getProperty(GROUP_NAME_PROPERTY),
                    GROUP_ID_PROPERTY, group.getKey().getName(),
                    GROUP_DESCRIPTION_PROPERTY, (String) group.getProperty(GROUP_DESCRIPTION_PROPERTY),
                    // GROUP_CALENDARID_PROPERTY, (String) group.getProperty(GROUP_CALENDARID_PROPERTY),
                    GROUP_CALENDARID_PROPERTY, "calendarID",
                    GROUP_IMAGE_PROPERTY, (String) group.getProperty(GROUP_IMAGE_PROPERTY));
                groupsListBuilder.add(groupMap);
            } catch (EntityNotFoundException invalidGroup) {
                continue;
            }
        }
        return groupsListBuilder.build(); 
    }
}
