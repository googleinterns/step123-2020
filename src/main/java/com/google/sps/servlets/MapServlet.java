package com.google.sps.servlets;

import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
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

        // Hard coded user groups for testing, 
        // Will get groups from user/datastore after functionality is verified
        ImmutableMap<String, String> groupMap = ImmutableMap.of("groupName", "Black Lives Matter",
            "groupID", "123");
        ImmutableMap<String, String> groupMap2 = ImmutableMap.of("groupName", "Sierra Club",
            "groupID", "456");
        ImmutableMap<String, ImmutableList<ImmutableMap<String, String>>> data = ImmutableMap.of("groups", ImmutableList.of(groupMap, groupMap2));

        SoyFileSet sfs = SoyFileSet
            .builder()
            .add(new File("../../src/main/java/templates/mapPages.soy"))
            .build();
        SoyTofu tofu = sfs.compileToTofu();
        
        String out = tofu
            .newRenderer("templates.mapPages.mapPage")
            .setData(data)
            .render();
        response.getWriter().println(out);
    }
}
