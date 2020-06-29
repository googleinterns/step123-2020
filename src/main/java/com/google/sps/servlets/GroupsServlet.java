package com.google.sps.servlets;

import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/groups")
public class GroupsServlet extends HttpServlet {
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, String> data = new HashMap<>();

        // Hard coded data for now but will use data from datastore
        // groups are added to datastore
        data.put("groupImage", "https://images.unsplash.com/photo-1591622414912-34f2a8f8172e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1950&q=80");
        data.put("groupName", "Black Lives Matter");
        data.put("groupDescription", "Group description would go here...");

        SoyFileSet sfs = SoyFileSet
            .builder()
            .add(new File("../../src/main/java/templates/pages.soy"))
            .build();
        SoyTofu tofu = sfs.compileToTofu();

        String out = tofu.newRenderer("templates.pages.groupsPage").setData(data).render();
        response.getWriter().println(out);
    }
}