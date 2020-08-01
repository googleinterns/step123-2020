package com.google.sps.utils;

import static com.google.sps.utils.StringConstants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.common.collect.ImmutableMap;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public final class SoyRendererUtils {
    private static final ClassLoader CLASS_LOADER = SoyRendererUtils.class.getClassLoader();

    /**
     * Returns the output string for the response. In other words,
     * it sets up the soy template with the passed in data.
     */
    public static String getOutputString(String templateFile, String templateNamespace, 
        ImmutableMap templateData) {
        SoyFileSet sfs = SoyFileSet
            .builder()
            .add(new File(CLASS_LOADER.getResource(templateFile).getFile()))
            .build();
        SoyTofu tofu = sfs.compileToTofu();

        return tofu.newRenderer(templateNamespace).setData(templateData).render();
    }

    public static List<Long> getGroupIdList(HttpServletRequest request) {
        // String userEmail = request.getUserPrincipal().getName();
        // This is just a hardcoded text email
        String userEmail = "example@test.com";
        // Stores the IDs for all the groups the user ahs joined
        List<Long> userGroups = new ArrayList<Long>();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query userQuery = new Query(USER_KIND).addFilter(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, 
            KeyFactory.createKey(USER_KIND, userEmail));
        PreparedQuery userPreparedQuery = datastore.prepare(userQuery);
        Entity user = userPreparedQuery.asSingleEntity();

        if (user != null) {
            userGroups = (List<Long>) user.getProperty(GROUPS_KEY);

        } else {
            // if the user does not exist, create one and add to datastore
            user = new Entity(USER_KIND, userEmail);

            user.setProperty(USER_EMAIL_PROPERTY, userEmail);
            // Hardcoded group added to user's list
            userGroups.add(123L);
            user.setProperty(GROUPS_KEY, userGroups);

            datastore.put(user);
        }

        return userGroups;
    }
}
