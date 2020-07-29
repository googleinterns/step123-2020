package com.google.sps.servlets;

import static com.google.sps.utils.SoyRendererUtils.getOutputString;
import static com.google.sps.utils.StringConstants.*;

import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
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

        // Reads API Key and HTTP referer from file
        ClassLoader classLoader = MapServlet.class.getClassLoader();
        File apiKeyFile = new File(classLoader.getResource(KEYS_TXT_FILE).getFile());
        Scanner scanner = new Scanner(apiKeyFile);
        final String apiKey = scanner.nextLine();
        scanner.close();

        // Hard coded user groups for testing, 
        // Will get groups from user/datastore after functionality is verified
        ImmutableMap<String, String> groupMap = ImmutableMap.of("groupName", "Black Lives Matter",
            "groupID", "123");
        ImmutableMap<String, String> groupMap2 = ImmutableMap.of("groupName", "Sierra Club",
            "groupID", "456");

        final String mapPageHtml = getOutputString(MAP_SOY_FILE, MAP_TEMPLATE_NAMESPACE, 
            ImmutableMap.of(GROUPS_KEY, ImmutableList.of(groupMap, groupMap2), "key", apiKey));

        response.getWriter().println(mapPageHtml);
    }
}
