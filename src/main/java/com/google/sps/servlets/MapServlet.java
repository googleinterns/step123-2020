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

        SoyFileSet sfs = SoyFileSet
            .builder()
            .add(new File("../../src/main/java/templates/mapPages.soy"))
            .build();
        SoyTofu tofu = sfs.compileToTofu();

        // Hard coded user groups for testing, 
        // Will get groups from user/datastore after functionality is verified
        String out = tofu
            .newRenderer("templates.mapPages.mapPage")
            .setData(ImmutableMap.of("groupNames", ImmutableList.of("Group One", "Group Two", "Group Three")))
            .render();
        response.getWriter().println(out);
    }
}
