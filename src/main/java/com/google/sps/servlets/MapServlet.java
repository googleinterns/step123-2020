package com.google.sps.servlets;

import static com.google.sps.utils.ServletUtils.getGroupsList;
import static com.google.sps.utils.SoyRendererUtils.getOutputString;
import static com.google.sps.utils.StringConstants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
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

        String userEmail = request.getUserPrincipal().getName();
        ImmutableList<ImmutableMap<String, String>> groupsList = getGroupsList(userEmail); 

        final String mapPageHtml = getOutputString(MAP_SOY_FILE, MAP_TEMPLATE_NAMESPACE, 
            ImmutableMap.of(GROUPS_KEY, groupsList, API_KEY_NAME, apiKey));
 
        response.getWriter().println(mapPageHtml);
    }
}
